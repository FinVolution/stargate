package com.ppdai.stargate.controller;

import java.util.ArrayList;
import java.util.List;

import com.ppdai.stargate.constant.OperatorType;
import com.ppdai.stargate.dto.*;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.*;
import com.ppdai.stargate.service.cloud.CloudInstanceService;
import com.ppdai.stargate.utils.NamingUtil;
import com.ppdai.stargate.vi.*;
import com.ppdai.stargate.vo.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.po.GroupEntity;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/groups")
@Slf4j
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupLogService groupLogService;

    @Autowired
    private JobService jobService;

    @Autowired
    private CloudInstanceService cloudInstanceService;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private AppService appService;

    @Autowired
    private ZoneService zoneService;

    /**
     * 获取指定站点的发布组列表
     *
     * @return 返回发布组实体列表
     */
    @RequestMapping(method = RequestMethod.GET)
    public Response<List<DeployGroupInfoVO>> findGroups(@RequestParam(value = "env") String env, @RequestParam(value = "appId") String appId) {
        List<DeployGroupInfoVO> groups = groupService.listGroupByEnvAndAppId(env, appId);
        return Response.mark(MessageType.SUCCESS, groups);
    }

    /**
     * 获取发布组信息
     *
     * @param groupId 发布组ID
     * @return 返回指定发布组的相关信息
     */
    @RequestMapping(value = "/{groupId}", method = RequestMethod.GET)
    public Response<GroupStatusVO> fetchGroupStatus(@PathVariable Long groupId) {
        GroupStatusVO groupStatusVO = groupService.fetchGroupStatus(groupId);
        return Response.success(groupStatusVO);
    }

    /**
     * 创建发布组并启动部署任务
     *
     * @param group  待保存的发布组信息
     * @return 返回新建好的发布组信息
     */
    @RequestMapping(method = RequestMethod.POST)
    public Response<String> startDeployGroup(@RequestBody AddGroupVI group) {
        String zone = group.getZone();
        if (StringUtils.isEmpty(zone)) {
            throw BaseException.newException(MessageType.ERROR, "指定的部署区域 [zone] 不存在或为空。");
        }

        // 检查配额
        groupService.checkSiteQuota(group.getAppId(), group.getEnv(), group.getInstanceCount(), group.getInstanceSpec());

        // 创建发布组，为下一步执行部署任务做准备
        Long groupId = null;
        GroupEntity groupEntity = groupService.createGroup(group);
        if (groupEntity != null) {
            groupId = groupEntity.getId();
        }

        if (groupId == null) {
            throw BaseException.newException(MessageType.ERROR, "发布组创建失败，请检查发布组参数是否输入正确。");
        }

        ApplicationEntity applicationEntity = appService.getAppByCmdbId(group.getAppId());

        if (applicationEntity == null) {
            throw BaseException.newException(MessageType.ERROR, "发布组创建失败，无法找到所属应用。");
        }

        log.info("<<startDeployGroup>> 开始添加发布组任务");
        MDC.put("Group", groupEntity.getName());

        List<Long> jobIdList = new ArrayList<>();
        StringBuilder errors = new StringBuilder();

        //todo
        String envVars = "{}";

        if (StringUtils.isNotEmpty(group.getStaticResources())) {
            // 使用静态资源发布，需要传zone和ip
            String[] resources = group.getStaticResources().split(",");
            for (int i = 0; i < resources.length; i++) {
                String[] splits = resources[i].split("@");
                String resourceZone = splits[0];
                String resourceIp = splits[1];
                String instanceName = instanceService.formatInstanceName(groupEntity.getName(), i);

                // 调用私有云接口部署实例
                DeployInstanceRequest deployInstanceRequest = new DeployInstanceRequest();
                deployInstanceRequest.setName(instanceName);
                deployInstanceRequest.setEnv(group.getEnv());
                deployInstanceRequest.setAppId(applicationEntity.getCmdbAppId());
                deployInstanceRequest.setPort(groupEntity.getPortCount());
                deployInstanceRequest.setSpec(groupEntity.getInstanceSpec());
                deployInstanceRequest.setImage(groupEntity.getReleaseTarget());
                deployInstanceRequest.setEnvVars(envVars);
                deployInstanceRequest.setZone(resourceZone);
                deployInstanceRequest.setIp(resourceIp);
                deployInstanceRequest.setNamespace(NamingUtil.formatNamespace(applicationEntity.getDepartmentCode()));
                deployInstanceRequest.setGroupId(groupEntity.getId());

                DeployInstanceResponse deployInstanceResponse = cloudInstanceService.deploy(deployInstanceRequest);

                if (deployInstanceResponse.getCode() != -1) {
                    jobIdList.add(deployInstanceResponse.getJobId());
                } else {
                    errors.append("[instance=").append(instanceName).append(",err=").append(deployInstanceResponse.getMsg()).append("]");
                }
            }
        } else {
            // 使用动态资源发布，无需传ip，按实例数均匀分配zone
            List<String> zoneList = zoneService.getZoneListByInstanceCount(group.getEnv(), zone, group.getInstanceCount(), groupId);

            for (int i = 0; i < group.getInstanceCount(); i++) {
                String instanceName = instanceService.formatInstanceName(groupEntity.getName(), i);

                // 调用私有云接口部署实例
                DeployInstanceRequest deployInstanceRequest = new DeployInstanceRequest();
                deployInstanceRequest.setName(instanceName);
                deployInstanceRequest.setEnv(group.getEnv());
                deployInstanceRequest.setAppId(applicationEntity.getCmdbAppId());
                deployInstanceRequest.setPort(groupEntity.getPortCount());
                deployInstanceRequest.setSpec(groupEntity.getInstanceSpec());
                deployInstanceRequest.setImage(groupEntity.getReleaseTarget());
                deployInstanceRequest.setEnvVars(envVars);
                deployInstanceRequest.setZone(zoneList.get(i));
                deployInstanceRequest.setNamespace(NamingUtil.formatNamespace(applicationEntity.getDepartmentCode()));
                deployInstanceRequest.setGroupId(groupEntity.getId());

                DeployInstanceResponse deployInstanceResponse = cloudInstanceService.deploy(deployInstanceRequest);

                if (deployInstanceResponse.getCode() != -1) {
                    jobIdList.add(deployInstanceResponse.getJobId());
                } else {
                    errors.append("[instance=").append(instanceName).append(",err=").append(deployInstanceResponse.getMsg()).append("]");
                }
            }
        }

        if (jobIdList.size() > 0) {
            // 创建job，用于同步私有云实例job状态
            jobService.addSyncInstanceJob(OperatorType.CREATE_GROUP.name(), groupId, jobIdList);

            log.info("<<startDeployGroup>> 添加发布组任务完成");
        }
        MDC.remove("Group");

        if (errors.length() > 0) {
            throw BaseException.newException(MessageType.ERROR, "部署实例失败，失败实例：" + errors.toString());
        }

        return Response.mark(MessageType.SUCCESS, "已进入任务队列：为发布组（%s）创建部署任务。", groupEntity.getName());
    }


    /**
     * 对指定发布组进行容量调整，扩容缩容到指定大小
     */
    @RequestMapping(value = "/resize", method = RequestMethod.POST)
    public Response<String> resizeGroup(@RequestBody ResizeGroupVI resizeGroupVI) {
        Long groupId = resizeGroupVI.getGroupId();
        String operatorType = resizeGroupVI.getOperatorType().name();
        Integer instanceCount = resizeGroupVI.getInstanceCount();
        String instanceNames = resizeGroupVI.getInstanceNames();
        String image = resizeGroupVI.getImage();
        String zone = resizeGroupVI.getZone();
        String staticResources = resizeGroupVI.getStaticResources();

        if (operatorType == null) {
            throw BaseException.newException(MessageType.ERROR, "指定的操作类型 [operatorType] 不存在。");
        }

        if (instanceCount == null) {
            throw BaseException.newException(MessageType.ERROR, "指定发布组内的操作实例个数 [instanceCount] 不存在。");
        }

        if (instanceCount <= 0) {
            throw BaseException.newException(MessageType.ERROR, "操作的实例数须大于0");
        }

        GroupEntity existingGroup = groupService.getGroupById(groupId);
        if (existingGroup == null) {
            throw BaseException.newException(MessageType.ERROR, "指定的发布组（id=%s）不存在。", groupId);
        }

        ApplicationEntity applicationEntity = appService.getAppByCmdbId(resizeGroupVI.getAppId());
        if (applicationEntity == null) {
            throw BaseException.newException(MessageType.ERROR, "指定的应用（appId=%s）不存在。", resizeGroupVI.getAppId());
        }

        MDC.put("Group", existingGroup.getName());
        log.info("<<resizeGroup>> starting resize instances task");

        List<Long> jobIdList = new ArrayList<>();
        StringBuilder errors = new StringBuilder();

        // 扩容时检查镜像和部署区域是否为空，并检查配额，然后调用私有云接口部署实例
        if (operatorType.equals(OperatorType.EXPAND_GROUP.name())) {
            if (StringUtils.isEmpty(image)) {
                throw BaseException.newException(MessageType.ERROR, "指定的镜像 [image] 不存在或为空。");
            }
            if (StringUtils.isEmpty(zone)) {
                throw BaseException.newException(MessageType.ERROR, "指定的部署区域 [zone] 不存在或为空。");
            }

            if (!image.startsWith(resizeGroupVI.getAppName())) {
                throw BaseException.newException(MessageType.ERROR, "目标镜像[%s]不属于该应用，请重新选取镜像发布。", image);
            }

            // 检查配额
            groupService.checkSiteQuota(resizeGroupVI.getAppId(), resizeGroupVI.getEnv(), instanceCount, existingGroup.getInstanceSpec());

            //todo
            String envVars = "{}";

            List<InstanceEntity> allInstanceEntities = instanceService.getInstancesByGroupIdEx(groupId);

            if (StringUtils.isNotEmpty(staticResources)) {
                // 使用静态资源发布，需要传zone和ip
                String[] resources = staticResources.split(",");

                for (int i = 0; i < resources.length; i++) {
                    String[] splits = resources[i].split("@");
                    String resourceZone = splits[0];
                    String resourceIp = splits[1];
                    String instanceName = instanceService.formatInstanceName(
                            existingGroup.getName(), i + allInstanceEntities.size());

                    // 调用私有云接口部署实例
                    DeployInstanceRequest deployInstanceRequest = new DeployInstanceRequest();
                    deployInstanceRequest.setName(instanceName);
                    deployInstanceRequest.setEnv(resizeGroupVI.getEnv());
                    deployInstanceRequest.setAppId(resizeGroupVI.getAppId());
                    deployInstanceRequest.setPort(existingGroup.getPortCount());
                    deployInstanceRequest.setSpec(existingGroup.getInstanceSpec());
                    deployInstanceRequest.setImage(image);
                    deployInstanceRequest.setEnvVars(envVars);
                    deployInstanceRequest.setZone(resourceZone);
                    deployInstanceRequest.setIp(resourceIp);
                    deployInstanceRequest.setNamespace(NamingUtil.formatNamespace(applicationEntity.getDepartmentCode()));
                    deployInstanceRequest.setGroupId(groupId);

                    DeployInstanceResponse deployInstanceResponse = cloudInstanceService.deploy(deployInstanceRequest);

                    if (deployInstanceResponse.getCode() != -1) {
                        jobIdList.add(deployInstanceResponse.getJobId());
                    } else {
                        errors.append("[instance=").append(instanceName).append(",err=").append(deployInstanceResponse.getMsg()).append("]");
                    }
                }
            } else {
                // 使用动态资源发布，无需传ip，按实例数均匀分配zone
                List<String> zoneList = zoneService.getZoneListByInstanceCount(resizeGroupVI.getEnv(), zone, instanceCount, groupId);

                for (int i = allInstanceEntities.size(); i < allInstanceEntities.size() + instanceCount; i++) {
                    String instanceName = instanceService.formatInstanceName(existingGroup.getName(), i);

                    // 调用私有云接口部署实例
                    DeployInstanceRequest deployInstanceRequest = new DeployInstanceRequest();
                    deployInstanceRequest.setName(instanceName);
                    deployInstanceRequest.setEnv(resizeGroupVI.getEnv());
                    deployInstanceRequest.setAppId(resizeGroupVI.getAppId());
                    deployInstanceRequest.setPort(existingGroup.getPortCount());
                    deployInstanceRequest.setSpec(existingGroup.getInstanceSpec());
                    deployInstanceRequest.setImage(image);
                    deployInstanceRequest.setEnvVars(envVars);
                    deployInstanceRequest.setZone(zoneList.get(i - allInstanceEntities.size()));
                    deployInstanceRequest.setNamespace(NamingUtil.formatNamespace(applicationEntity.getDepartmentCode()));
                    deployInstanceRequest.setGroupId(groupId);

                    DeployInstanceResponse deployInstanceResponse = cloudInstanceService.deploy(deployInstanceRequest);

                    if (deployInstanceResponse.getCode() != -1) {
                        jobIdList.add(deployInstanceResponse.getJobId());
                    } else {
                        errors.append("[instance=").append(instanceName).append(",err=").append(deployInstanceResponse.getMsg()).append("]");
                    }
                }
            }
        }

        // 缩容时检查实例是否为空，是否接入流量，然后调用私有云接口销毁实例
        if (operatorType.equals(OperatorType.REDUCE_GROUP.name())) {
            if (StringUtils.isEmpty(instanceNames)) {
                throw BaseException.newException(MessageType.ERROR, "指定发布组内的目标实例 [instanceNames] 不存在或为空。");
            }

            // 检查实例是否属于发布组
            groupService.checkGroupInstances(groupId, instanceNames);

            // 检查实例是否接入流量
            groupService.checkPullInInstances(existingGroup, instanceNames);

            String[] instanceNameList = instanceNames.split(",");
            for (String instance : instanceNameList) {
                // 调用私有云接口销毁实例
                DestroyInstanceRequest destroyInstanceRequest = new DestroyInstanceRequest();
                destroyInstanceRequest.setName(instance);
                DestroyInstanceResponse destroyInstanceResponse = cloudInstanceService.destroy(destroyInstanceRequest);

                if (destroyInstanceResponse.getCode() != -1) {
                    jobIdList.add(destroyInstanceResponse.getJobId());
                } else {
                    errors.append("[instance=").append(instance).append(",err=").append(destroyInstanceResponse.getMsg()).append("]");
                }
            }
        }

        if (jobIdList.size() > 0) {
            // 创建job，用于同步私有云实例job状态
            jobService.addSyncInstanceJob(operatorType, groupId, jobIdList);

            log.info("<<resizeGroup>> resize instances task async done, groupId={}, operatorType={}", groupId, operatorType);
        }
        MDC.remove("Group");

        if (errors.length() > 0) {
            if (operatorType.equals(OperatorType.EXPAND_GROUP.name())) {
                throw BaseException.newException(MessageType.ERROR, "部署实例失败，失败实例：" + errors.toString());
            } else if (operatorType.equals(OperatorType.REDUCE_GROUP.name())) {
                throw BaseException.newException(MessageType.ERROR, "销毁实例失败，失败实例：" + errors.toString());
            }
        }

        return Response.mark(MessageType.SUCCESS, "已进入任务队列：调整发布组（%s）容量。", existingGroup.getName());
    }

    /**
     * 对指定发布组进行流量调整，拉入或拉出指定实例的流量
     */
    @RequestMapping(value = "/pull_instance", method = RequestMethod.POST)
    public Response<String> pullInstance(@RequestBody PullInstanceVI pullInstanceVI) {
        Long groupId = pullInstanceVI.getGroupId();
        String operatorType = pullInstanceVI.getOperatorType().name();
        String instanceNames = pullInstanceVI.getInstanceNames();

        if (operatorType == null) {
            throw BaseException.newException(MessageType.ERROR, "指定的操作类型 [operatorType] 不存在。");
        }

        if (StringUtils.isEmpty(instanceNames)) {
            throw BaseException.newException(MessageType.ERROR, "指定发布组内的目标实例 [instanceNames] 不存在或为空。");
        }
        
        GroupEntity existingGroup = groupService.getGroupById(groupId);
        if (existingGroup == null) {
            throw BaseException.newException(MessageType.ERROR, "指定的发布组（id=%s）不存在。", groupId);
        }

        MDC.put("Group", existingGroup.getName());
        log.info("<<pullInstance>> starting pull instances task, groupId={}, operatorType={}, instanceNames={}", groupId, operatorType, instanceNames);

        // 检查实例是否属于发布组
        groupService.checkGroupInstances(groupId, instanceNames);

        List<Long> jobIdList = new ArrayList<>();
        StringBuilder errors = new StringBuilder();

        String[] instanceNameList = instanceNames.split(",");
        for (String instance : instanceNameList) {
            if (operatorType.equals(OperatorType.PULL_IN.name())) {
                // 调用私有云接口上线实例
                UpInstanceRequest upInstanceRequest = new UpInstanceRequest();
                upInstanceRequest.setName(instance);
                UpInstanceResponse response = cloudInstanceService.up(upInstanceRequest);
                if (response.getCode() != -1) {
                    jobIdList.add(response.getJobId());
                } else {
                    errors.append("[instance=").append(instance).append(",err=").append(response.getMsg()).append("]");
                }
            } else if (operatorType.equals(OperatorType.PULL_OUT.name())) {
                // 调用私有云接口下线实例
                DownInstanceRequest downInstanceRequest = new DownInstanceRequest();
                downInstanceRequest.setName(instance);
                DownInstanceResponse response = cloudInstanceService.down(downInstanceRequest);
                if (response.getCode() != -1) {
                    jobIdList.add(response.getJobId());
                } else {
                    errors.append("[instance=").append(instance).append(",err=").append(response.getMsg()).append("]");
                }
            }
        }

        if (jobIdList.size() > 0) {
            // 创建job，用于同步私有云实例job状态
            jobService.addSyncInstanceJob(operatorType, groupId, jobIdList);

            log.info("<<pullInstance>> pull instances task async done, groupId={}, operatorType={}, instanceNames={}", groupId, operatorType, instanceNames);
        }
        MDC.remove("Group");

        if (errors.length() > 0) {
            if (operatorType.equals(OperatorType.PULL_IN.name())) {
                throw BaseException.newException(MessageType.ERROR, "上线实例失败，失败实例：" + errors.toString());
            } else if (operatorType.equals(OperatorType.PULL_OUT.name())) {
                throw BaseException.newException(MessageType.ERROR, "下线实例失败，失败实例：" + errors.toString());
            }
        }

        return Response.mark(MessageType.SUCCESS, "已进入任务队列：调整发布组（%s）实例流量状态。", existingGroup.getName());
    }

    /**
     * 更新发布组中的指定实例
     */
    @RequestMapping(value = "/update_instance", method = RequestMethod.POST)
    public Response<String> updateInstance(@RequestBody UpdateInstanceVI updateInstanceVI) {
        Long groupId = updateInstanceVI.getGroupId();
        String instanceNames = updateInstanceVI.getInstanceNames();
        String image = updateInstanceVI.getImage();

        if (StringUtils.isEmpty(instanceNames)) {
            throw BaseException.newException(MessageType.ERROR, "指定发布组内的目标实例 [instanceNames] 不存在或为空。");
        }

        if (StringUtils.isEmpty(image)) {
            throw BaseException.newException(MessageType.ERROR, "指定的镜像 [image] 不存在或为空。");
        }

        GroupEntity existingGroup = groupService.getGroupById(groupId);
        if (existingGroup == null) {
            throw BaseException.newException(MessageType.ERROR, "指定的发布组（id=%s）不存在。", groupId);
        }

        if (!image.startsWith(updateInstanceVI.getAppName())) {
            throw BaseException.newException(MessageType.ERROR, "目标镜像[%s]不属于该应用，请重新选取镜像发布。", image);
        }

        MDC.put("Group", existingGroup.getName());
        log.info("<<updateInstance>> starting update instances task, groupId={}, instanceNames={}, image={}", groupId, instanceNames, image);

        // 检查实例是否属于发布组
        groupService.checkGroupInstances(groupId, instanceNames);

        // 检查实例是否接入流量
        groupService.checkPullInInstances(existingGroup, instanceNames);

        List<Long> jobIdList = new ArrayList<>();
        StringBuilder errors = new StringBuilder();

        String[] instanceNameList = instanceNames.split(",");
        for (String instance : instanceNameList) {
            // 调用私有云接口更新实例
            UpdateInstanceRequest updateInstanceRequest = new UpdateInstanceRequest();
            updateInstanceRequest.setName(instance);
            updateInstanceRequest.setImage(updateInstanceVI.getImage());
            UpdateInstanceResponse response = cloudInstanceService.update(updateInstanceRequest);
            if (response.getCode() != -1) {
                jobIdList.add(response.getJobId());
            } else {
                errors.append("[instance=").append(instance).append(",err=").append(response.getMsg()).append("]");
            }

        }

        if (jobIdList.size() > 0) {
            // 创建job，用于同步私有云实例job状态
            jobService.addSyncInstanceJob(OperatorType.UPDATE_INSTANCE.name(), groupId, jobIdList);

            log.info("<<updateInstance>> update instances task async done, groupId={}, instanceNames={}, image={}", groupId, instanceNames, image);
        }
        MDC.remove("Group");

        if (errors.length() > 0) {
            throw BaseException.newException(MessageType.ERROR, "更新实例失败，失败实例：" + errors.toString());
        }

        return Response.mark(MessageType.SUCCESS, "已进入任务队列：更新发布组（%s）实例。", existingGroup.getName());
    }

    /**
     * 重启发布组中的指定实例
     */
    @RequestMapping(value = "/restart_instance", method = RequestMethod.POST)
    public Response<String> restartInstance(@RequestBody RestartInstanceVI restartInstanceVI) {
        Long groupId = restartInstanceVI.getGroupId();
        String instanceNames = restartInstanceVI.getInstanceNames();

        if (StringUtils.isEmpty(instanceNames)) {
            throw BaseException.newException(MessageType.ERROR, "指定发布组内的目标实例 [instanceNames] 不存在或为空。");
        }

        GroupEntity existingGroup = groupService.getGroupById(groupId);
        if (existingGroup == null) {
            throw BaseException.newException(MessageType.ERROR, "指定的发布组（id=%s）不存在。", groupId);
        }

        MDC.put("Group", existingGroup.getName());
        log.info("<<restartInstance>> starting restart instances task, groupId={}, instanceNames={}", groupId, instanceNames);

        // 检查实例是否属于发布组
        groupService.checkGroupInstances(groupId, instanceNames);

        // 检查实例是否接入流量
        groupService.checkPullInInstances(existingGroup, instanceNames);

        List<Long> jobIdList = new ArrayList<>();
        StringBuilder errors = new StringBuilder();

        String[] instanceNameList = instanceNames.split(",");
        for (String instance : instanceNameList) {
            // 调用私有云接口更新实例
            RestartInstanceRequest restartInstanceRequest = new RestartInstanceRequest();
            restartInstanceRequest.setName(instance);
            RestartInstanceResponse response = cloudInstanceService.restart(restartInstanceRequest);
            if (response.getCode() != -1) {
                jobIdList.add(response.getJobId());
            } else {
                errors.append("[instance=").append(instance).append(",err=").append(response.getMsg()).append("]");
            }
        }

        if (jobIdList.size() > 0) {
            // 创建job，用于同步私有云实例job状态
            jobService.addSyncInstanceJob(OperatorType.RESTART_INSTANCE.name(), groupId, jobIdList);

            log.info("<<restartInstance>> restart instances task async done, groupId={}, instanceNames={}", groupId, instanceNames);
        }
        MDC.remove("Group");

        if (errors.length() > 0) {
            throw BaseException.newException(MessageType.ERROR, "重启实例失败，失败实例：" + errors.toString());
        }

        return Response.mark(MessageType.SUCCESS, "已进入任务队列：重启发布组（%s）实例。", existingGroup.getName());
    }

    /**
     * 关闭部署任务并删除指定发布组
     *
     * @param groupId 指定groupId
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Response<String> stopDeployGroupById(@PathVariable("id") Long groupId) {

        GroupEntity existingGroup = groupService.getGroupById(groupId);
        if (existingGroup == null) {
            throw BaseException.newException(MessageType.ERROR, "指定的发布组（id=%s）不存在。", groupId);
        }

        MDC.put("Group", existingGroup.getName());
        log.info("<<stopDeployGroup>> starting stop deploy group task");

        List<Long> jobIdList = new ArrayList<>();
        StringBuilder errors = new StringBuilder();

        List<InstanceEntity> instances = instanceService.getInstancesByGroupId(groupId);
        for (int i = 0; i < instances.size(); i++) {
            InstanceEntity instanceEntity = instances.get(i);

            // 调用私有云接口销毁实例
            DestroyInstanceRequest destroyInstanceRequest = new DestroyInstanceRequest();
            destroyInstanceRequest.setName(instanceEntity.getName());
            DestroyInstanceResponse destroyInstanceResponse = cloudInstanceService.destroy(destroyInstanceRequest);

            if (destroyInstanceResponse.getCode() != -1) {
                jobIdList.add(destroyInstanceResponse.getJobId());
            } else {
                errors.append("[instance=").append(instanceEntity.getName()).append(",err=").append(destroyInstanceResponse.getMsg()).append("]");
            }
        }

        // 创建job，用于同步私有云实例job状态
        jobService.addSyncInstanceJob(OperatorType.REMOVE_GROUP.name(), groupId, jobIdList);

        log.info("<<stopDeployGroup>> stop deploy group task async done");
        MDC.remove("Group");

        if (errors.length() > 0) {
            throw BaseException.newException(MessageType.ERROR, "销毁实例失败，失败实例：" + errors.toString());
        }

        return Response.mark(MessageType.SUCCESS, "已进入任务队列：删除发布组（%s）。", existingGroup.getName());
    }

    /**
     * 获取指定发布组的发布状态
     *
     * @param groupId 指定groupId
     * @return
     */
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public Response<JobInfo> fetchGroupReleaseStatus(@RequestParam("groupId") Long groupId) {
        JobInfo jobInfo = groupService.fetchGroupReleaseStatus(groupId);
        return Response.success(jobInfo);
    }

    @RequestMapping(value = "/releaselog", method = RequestMethod.GET)
    public Response<GroupLogVO> getReleaseLog(@RequestParam(value = "groupName", required = true) String groupName,
                                              @RequestParam(value = "groupId", required = true) long groupId,
                                              @RequestParam(value = "jobId", required = false, defaultValue = "-1") long jobId,
                                              @RequestParam(value = "fromUI", required = false, defaultValue = "true") boolean fromUI) {
        GroupLogVO groupLogVO = groupLogService.getReleaseLog(groupName, groupId, jobId, fromUI);
        return Response.success(groupLogVO);
    }

    @RequestMapping(value = "/restartJob", method = RequestMethod.POST)
    public Response<String> restartJob(@RequestParam Long jobId) {
        jobService.restartJobById(jobId);
        return Response.success("任务开始重新执行, jobId=" + jobId);
    }
}
