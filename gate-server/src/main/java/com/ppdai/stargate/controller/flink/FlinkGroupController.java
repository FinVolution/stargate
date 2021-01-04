package com.ppdai.stargate.controller.flink;

import com.alibaba.fastjson.JSONObject;
import com.ppdai.stargate.constant.OperatorType;
import com.ppdai.stargate.controller.request.FlinkDeployRequest;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.dto.DeployInstanceRequest;
import com.ppdai.stargate.dto.DeployInstanceResponse;
import com.ppdai.stargate.dto.UpdateInstanceRequest;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.GroupEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.AppService;
import com.ppdai.stargate.service.GroupService;
import com.ppdai.stargate.service.InstanceService;
import com.ppdai.stargate.service.ZoneService;
import com.ppdai.stargate.service.cloud.CloudInstanceService;
import com.ppdai.stargate.service.flink.FlinkJobGroupService;
import com.ppdai.stargate.service.flink.FlinkJobService;
import com.ppdai.stargate.service.flink.FlinkService;
import com.ppdai.stargate.utils.NamingUtil;
import com.ppdai.stargate.utils.Strings;
import com.ppdai.stargate.vi.AddGroupVI;
import com.ppdai.stargate.vi.FlinkJobVO;
import com.ppdai.stargate.vo.ContainerLogVO;
import com.ppdai.stargate.vo.DeployGroupInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/flink/groups")
@Slf4j
public class FlinkGroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private FlinkJobGroupService flinkJobGroupService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private CloudInstanceService cloudInstanceService;

    @Autowired
    private FlinkJobService flinkJobService;

    @Autowired
    private FlinkService flinkService;
    @Autowired
    private AppService appService;


    /**
     * 获取指定站点的发布组列表
     *
     * @return 返回发布组实体列表
     */
    @RequestMapping(method = RequestMethod.GET)
    public Response<List<DeployGroupInfoVO>> findGroups(@RequestParam(value = "env") String env, @RequestParam(value = "appId") String appId) {
        List<DeployGroupInfoVO> groups = flinkJobGroupService.listGroupByEnvAndAppId(env, appId);
        return Response.mark(MessageType.SUCCESS, groups);
    }


    /**
     * 基于flink1.11 application mode方式
     *
     * @param siteId
     * @param group
     * @return
     */
    @RequestMapping(value = "/application", method = RequestMethod.POST)
    public Response<String> createGroup(@RequestParam(value = "siteId", required = false) Long siteId,
                                        @RequestBody AddGroupVI group) {

        String zone = group.getZone();
        if (StringUtils.isEmpty(zone)) {
            throw BaseException.newException(MessageType.ERROR, "指定的部署区域 [zone] 不存在或为空。");
        }

        // 检查配额
        Long groupId = null;
        groupService.checkSiteQuota(group.getAppId(), group.getEnv(), group.getInstanceCount(), group.getInstanceSpec());
        GroupEntity groupEntity = groupService.createGroup(group);
        if (groupEntity != null) {
            groupId = groupEntity.getId();
        }

        if (groupId == null) {
            throw BaseException.newException(MessageType.ERROR, "发布组创建失败，请检查发布组参数是否输入正确。");
        }

        // 创建发布组，为下一步执行部署任务做准备
        groupService.createGroup(group);
        log.info("<<startDeployGroup>> 开始添加发布组任务");
        MDC.put("Group", groupEntity.getName());
        StringBuilder errors = new StringBuilder();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("checkpoint", group.getCheckpoint());
        jsonObject.put("HADOOP_CLUSTER", group.getHadoopConfig());
        jsonObject.put("SESSION_CLUSTER_ID", groupEntity.getName().replace(".", "-"));
        jsonObject.put("INSTANCE_COUNT", group.getInstanceCount());
        String envVars = jsonObject.toJSONString();

        // 使用动态资源发布，无需传ip，按实例数均匀分配zone
        List<String> zoneList = zoneService.getZoneListByInstanceCount(group.getEnv(), zone, 1, groupId);
        String instanceName = instanceService.formatInstanceName(groupEntity.getName(), 0);
        ApplicationEntity applicationEntity = appService.getAppByCmdbId(group.getAppId());

        // 调用私有云接口部署实例
        DeployInstanceRequest deployInstanceRequest = new DeployInstanceRequest();
        deployInstanceRequest.setName(instanceName);
        deployInstanceRequest.setEnv(group.getEnv());
        deployInstanceRequest.setAppId(group.getAppId());
        deployInstanceRequest.setPort(groupEntity.getPortCount());
        deployInstanceRequest.setSpec(groupEntity.getInstanceSpec());
        deployInstanceRequest.setImage(groupEntity.getReleaseTarget());
        deployInstanceRequest.setEnvVars(envVars);
        deployInstanceRequest.setZone(zoneList.get(0));
        deployInstanceRequest.setNamespace(NamingUtil.formatNamespace(applicationEntity.getDepartmentCode()));
        deployInstanceRequest.setGroupId(groupEntity.getId());

        DeployInstanceResponse deployInstanceResponse = cloudInstanceService.deploy(deployInstanceRequest);

        if (deployInstanceResponse.getCode() == -1) {
            errors.append("[instance=").append(instanceName).append(",err=").append(deployInstanceResponse.getMsg()).append("]");
        }
        flinkJobService.addCreateGroupJob(OperatorType.CREATE_GROUP.name(), groupId, deployInstanceResponse.getInstance().getId());

        MDC.remove("Group");
        if (errors.length() > 0) {
            throw BaseException.newException(MessageType.ERROR, "添加实例失败，失败实例：" + errors.toString());
        }
        return Response.mark(MessageType.SUCCESS, "已进入任务队列：为发布组（%s）创建部署任务。", groupEntity.getName());
    }

    @RequestMapping(value = "/application/start-job", method = RequestMethod.POST)
    public Response<String> runJob(@RequestBody FlinkDeployRequest flinkDeployRequest) {
        GroupEntity groupEntity = groupService.getGroupById(flinkDeployRequest.getGroupId());

        List<InstanceEntity> instanceEntityList = instanceService.getInstancesByGroupId(flinkDeployRequest.getGroupId());

        InstanceEntity ctrlInstance = instanceEntityList.get(0);
        String envVars = ctrlInstance.getEnvVars();
        JSONObject jsonObject = Strings.stringToJsonObject(envVars);
        jsonObject.put("variable", flinkDeployRequest.getVariable());
        jsonObject.put("parallelism", flinkDeployRequest.getInstanceCount());
        jsonObject.put("taskSlots", 1);
        jsonObject.put("cmd", flinkDeployRequest.getCmd());
        ctrlInstance.setEnvVars(jsonObject.toJSONString());
        instanceService.saveInstance(ctrlInstance);
        flinkJobService.addDeployApplicationFlinkJob(OperatorType.DEPLOY_FLINKJOB.name(), flinkDeployRequest, ctrlInstance.getId());

        return Response.mark(MessageType.SUCCESS, "已进入任务队列：为发布组（%s）启动FlinkJob任务。", groupEntity.getName());
    }

    @RequestMapping(value = "/application/cancel-job", method = RequestMethod.POST)
    public Response<String> cancelJob(@RequestParam(value = "groupId") Long groupId,
                                      @RequestParam(value = "destroy") boolean destroy) {
        GroupEntity groupEntity = groupService.getGroupById(groupId);
        if (groupEntity == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定发布组[%s]不存在。", groupId);
            log.error("<<addStopFlinkJob>> " + ex.getMessage());
            throw ex;
        }
        List<InstanceEntity> instanceEntityList = instanceService.getInstancesByGroupId(groupId);

        InstanceEntity ctrlInstance = instanceEntityList.get(0);
        String envVars = ctrlInstance.getEnvVars();
        JSONObject jsonObject = Strings.stringToJsonObject(envVars);
        jsonObject.put("DESTROY", destroy);
        ctrlInstance.setEnvVars(jsonObject.toJSONString());
        instanceService.saveInstance(ctrlInstance);
        flinkJobService.addStopFlinkJob(OperatorType.STOP_FLINKJOB.name(), groupEntity, ctrlInstance.getId());

        return Response.mark(MessageType.SUCCESS, "已进入任务队列：为发布组（%s）停止FlinkJob任务。", groupEntity.getName());

    }


    @RequestMapping(value = "/updateJob", method = RequestMethod.POST)
    public Response<String> updateJob(@RequestBody FlinkDeployRequest flinkDeployRequest) {
        Long groupId = flinkDeployRequest.getGroupId();
        List<InstanceEntity> instanceEntityList = instanceService.getInstancesByGroupId(groupId);
        InstanceEntity ctrlInstance = instanceEntityList.get(0);
        UpdateInstanceRequest updateInstanceRequest = new UpdateInstanceRequest();
        updateInstanceRequest.setName(ctrlInstance.getName());
        updateInstanceRequest.setImage(flinkDeployRequest.getImage());
        ctrlInstance.setImage(flinkDeployRequest.getImage());
        String envVars = ctrlInstance.getEnvVars();
        JSONObject jsonObject = Strings.stringToJsonObject(envVars);
        jsonObject.put("variable", flinkDeployRequest.getVariable());
        jsonObject.put("cmd", flinkDeployRequest.getCmd());
        jsonObject.put("imageUrl", flinkDeployRequest.getReleaseTarget());
        ctrlInstance.setEnvVars(jsonObject.toJSONString());
        instanceService.saveInstance(ctrlInstance);
        flinkJobService.addUpdateFlinkJob(OperatorType.UPDATE_FLINKJOB.name(), groupId, ctrlInstance.getId(), flinkDeployRequest);
        return Response.mark(MessageType.SUCCESS, "已进入任务队列：为发布组更新FlinkJob任务。");
    }

    @RequestMapping(value = "/destroy/{groupId}", method = RequestMethod.POST)
    public Response<String> destroy(@PathVariable(value = "groupId") Long groupId) {
        GroupEntity existingGroup = groupService.getGroupById(groupId);
        if (existingGroup == null) {
            throw BaseException.newException(MessageType.ERROR, "指定的发布组（id=%s）不存在。", groupId);
        }


        MDC.put("Group", existingGroup.getName());
        log.info("<<stopDeployGroup>> starting stop deploy group task");

        List<InstanceEntity> instances = instanceService.getInstancesByGroupId(groupId);
        for (int i = 0; i < instances.size(); i++) {
            flinkJobService.addDestroyFlinkJob(OperatorType.DESTROY_FLINKJOB.name(), groupId, instances.get(i));
        }
        GroupEntity groupEntity = groupService.getGroupById(groupId);
        return Response.mark(MessageType.SUCCESS, "已进入任务队列：为发布组（%s）销毁FlinkJob任务。", groupEntity.getName());
    }

    @PostMapping(value = "/restart/{groupId}")
    public Response<String> restart(@PathVariable("groupId") Long groupId) {

        List<InstanceEntity> instanceEntityList = instanceService.getInstancesByGroupId(groupId);

        InstanceEntity ctrlInstance = instanceEntityList.get(0);

        log.info("<<restartJob>> 重启FlinkJob");

        UpdateInstanceRequest updateInstanceRequest = new UpdateInstanceRequest();
        updateInstanceRequest.setName(ctrlInstance.getName());
        flinkJobService.addRestartFlinkJob(OperatorType.RESTART_FLINKJOB.name(), ctrlInstance.getId(), groupId);
        return Response.mark(MessageType.SUCCESS, "已进入任务队列：为发布组（%s）重启FlinkJob任务。");
    }

    @RequestMapping(value = "/job", method = RequestMethod.GET)
    public Response<List<FlinkJobVO>> findInstancesByGroupId(@RequestParam(value = "groupId") Long groupId) {
        FlinkJobVO flinkJobVO = flinkService.getFlinkJobStatusByGroupId(groupId);
        return Response.mark(MessageType.SUCCESS, Arrays.asList(flinkJobVO));
    }

    @RequestMapping(value = "/containerlog", method = RequestMethod.GET)
    public Response<ContainerLogVO> getFlinkJobContainerLog(@RequestParam(value = "instanceName") String instanceName) {
        InstanceEntity instanceEntity = instanceService.findByName(instanceName);
        String logs = flinkService.getContainerLog(instanceEntity);
        ContainerLogVO containerLogVO = new ContainerLogVO();
        containerLogVO.setLogs(logs);
        return Response.success(containerLogVO);
    }
}
