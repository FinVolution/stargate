package com.ppdai.stargate.service;

import java.util.*;
import java.util.stream.Collectors;

import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.po.*;
import com.ppdai.stargate.remote.RemoteCmdb;
import com.ppdai.stargate.remote.RemoteRegistry;
import com.ppdai.stargate.remote.RemoteRegistryManager;
import com.ppdai.stargate.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.dao.GroupRepository;
import com.ppdai.stargate.dao.InstanceRepository;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.utils.ConvertUtil;
import com.ppdai.stargate.vi.AddGroupVI;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GroupService {

    @Autowired
    private JobService jobService;
    @Autowired
    private AppService appService;
    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private InstanceRepository instanceRepo;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private ZoneService zoneService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private RemoteCmdb remoteCmdb;
    @Autowired
    private RemoteRegistryManager remoteRegistryManager;

    /**
     * 获取指定站点的发布组列表
     *
     * @return 返回发布组实体列表
     */
    public List<DeployGroupInfoVO> listGroupByEnvAndAppId(String env, String appId) {
        List<DeployGroupInfoVO> deployGroupInfoVOs = new ArrayList<>();
        groupRepo.findByEnvAndAppId(env, appId).forEach(group -> {
            DeployGroupInfoVO groupVO = new DeployGroupInfoVO();
            BeanUtils.copyProperties(group, groupVO);

            // 获取各个发布组的流量状态
            Long groupId = group.getId();
            String environment = group.getEnvironment();
            List<InstanceEntity> instances = instanceRepo.findInstancesByGroupId(groupId);

            String domain = appService.getAppDomainByEnv(appId, env);

            RemoteRegistry remoteRegistry = remoteRegistryManager.getRemoteRegistryByDomain(domain);
            List<InstanceVO> instanceVOList = remoteRegistry.getInstanceStatus(environment, domain, instances);
            groupVO.setInstances(ConvertUtil.convert(instances, InstanceVO.class));
            groupVO.setInstanceCount(instances.size());

            Integer expected = 0;
            for (InstanceEntity instance : instances) {
                if (instance.getHasPulledIn()) {
                    expected++;
                }
            }
            groupVO.setExpectedCount(expected);

            // 计算实际流量
            Integer activeCount = 0;
            for (InstanceVO instanceVO : instanceVOList) {
                if (instanceVO.getStatus() != null && instanceVO.getStatus() == 1) {
                    activeCount++;
                }
            }

            // 注：total以发布系统的实例数为准，因为remoteRegistry注册中心的实例总数可能会有延迟，即已下线的实例但还在保留在注册中心里
            int total = groupVO.getInstanceCount();
            groupVO.setActiveCount(activeCount);
            if (total <= 0) {
                groupVO.setInstanceUpPercentage(0);
            } else {
                groupVO.setInstanceUpPercentage(100 * activeCount / total);
            }

            JobInfo jobInfo = jobService.getCurrentJobByGroupId(groupId);
            groupVO.setJobInfo(jobInfo);

            deployGroupInfoVOs.add(groupVO);

        });

        return deployGroupInfoVOs;
    }

    @Transactional
    public GroupEntity createGroup(AddGroupVI addGroupVI) {
        String releaseTarget = addGroupVI.getReleaseTarget();
        if (releaseTarget == null) {
            throw BaseException.newException(MessageType.ERROR, "请选择应用镜像");
        }

        if (!releaseTarget.startsWith(addGroupVI.getAppName())) {
            throw BaseException.newException(MessageType.ERROR, "目标镜像[%s]不属于该应用，请重新选取镜像发布。", releaseTarget);
        }

        String instanceSpec = addGroupVI.getInstanceSpec();
        if (instanceSpec == null) {
            throw BaseException.newException(MessageType.ERROR, "请选择发布规格");
        }

        if (addGroupVI.getInstanceCount() <= 0) {
            throw BaseException.newException(MessageType.ERROR, "发布组实例个数须大于0");
        }

        GroupEntity groupEntity = ConvertUtil.convert(addGroupVI, GroupEntity.class);

        String newGroupName = formatNextSiteGroupName(addGroupVI.getAppId());
        groupEntity.setName(newGroupName);
        groupEntity.setEnvironment(addGroupVI.getEnv());
        groupEntity.setId(null);
        return groupRepo.saveAndFlush(groupEntity);
    }

    /**
     * 缩容时检查实例是否接入流量
     * 操作实例时检查实例是否接入流量
     * @param groupEntity
     * @param instanceNames 实例名列表
     */
    public void checkPullInInstances(GroupEntity groupEntity, String instanceNames) {
        List<InstanceEntity> instanceEntities = instanceRepo.findInstancesByGroupId(groupEntity.getId());

        String domain = appService.getAppDomainByEnv(groupEntity.getAppId(), groupEntity.getEnvironment());

        RemoteRegistry remoteRegistry = remoteRegistryManager.getRemoteRegistryByDomain(domain);

        // 查询出接入流量的实例列表
        List<InstanceVO> instanceVOS = remoteRegistry.getInstanceStatus(groupEntity.getEnvironment(), domain, instanceEntities)
                .stream().filter(x -> x.getStatus() != null && x.getStatus() == 1).collect(Collectors.toList());

        String[] instanceNameList = instanceNames.split(",");

        for (InstanceVO instanceVO : instanceVOS) {
            for (String instanceName : instanceNameList) {
                if (instanceName.equals(instanceVO.getName())) {
                    throw BaseException.newException(MessageType.ERROR, "操作实例前请将流量拉出, name=" + instanceName);
                }
            }
        }
    }

    /**
     * 操作发布组实例时检查实例是否属于发布组
     * @param groupId
     * @param instanceNames
     */
    public void checkGroupInstances(Long groupId, String instanceNames) {
        List<InstanceEntity> instances = instanceService.getInstancesByGroupId(groupId);
        String[] instanceNameList = instanceNames.split(",");
        for (String instanceName : instanceNameList) {
            Optional<InstanceEntity> instanceOptional = instances.stream().filter(x -> x.getName().equals(instanceName)).findFirst();
            if (!instanceOptional.isPresent()) {
                throw BaseException.newException(MessageType.ERROR, "实例不存在或不属于该发布组, name=" + instanceName + ", groupId=" + groupId);
            }
        }
    }

    /**
     * 检查站点的配额是否足够
     *
     * @param countOfInstanceApply 站点待申请的实例配额
     * @param instanceSpec         发布的实例规格
     */
    public void checkSiteQuota(String appId, String environment, Integer countOfInstanceApply, String instanceSpec) {

        List<AppQuotaStatusVO> appQuotaStatusVOS = appService.fetchAppQuotaStatusByAppAndEnv(appId, environment);

        Optional<AppQuotaStatusVO> appQuotaStatusVO = appQuotaStatusVOS.stream().filter(x -> x.getSpectypeName().equals(instanceSpec)).findFirst();

        if (!appQuotaStatusVO.isPresent()) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "获取配额失败, 请检查应用是否创建了该规格的配额");
            log.error("<<checkSiteQuota>> " + ex.getMessage());
            throw ex;
        } else if (countOfInstanceApply > appQuotaStatusVO.get().getFreeCount()) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "应用配额不足, 环境: %s, 规格: %s, 总额: %d, 已使用: %d, 申请数: %d",
                    environment, instanceSpec, appQuotaStatusVO.get().getTotal(), appQuotaStatusVO.get().getUsedCount(), countOfInstanceApply);
            log.error("<<checkSiteQuota>> " + ex.getMessage());
            throw ex;
        }

        log.info("<<checkSiteQuota>> appId={}, env={}, spec={}, total={}, used={}, apply={}",
                appId, environment, instanceSpec, appQuotaStatusVO.get().getTotal(), appQuotaStatusVO.get().getUsedCount(), countOfInstanceApply);
    }

    /**
     * 获取发布组当前发布状态
     *
     * @param groupId 发布组ID
     * @return 发布组的发布状态，若group不存在，返回空发布组信息
     */
    public JobInfo fetchGroupReleaseStatus(Long groupId) {
        return jobService.getCurrentJobByGroupId(groupId);
    }

    public GroupStatusVO fetchGroupStatus(Long groupId) {
        GroupStatusVO groupStatusVO = new GroupStatusVO();

        GroupEntity group = groupRepo.findOne(groupId);
        if (group != null) {
            groupStatusVO.setGroupId(groupId);
            groupStatusVO.setGroupName(group.getName());
            groupStatusVO.setReleaseTarget(group.getReleaseTarget());
            groupStatusVO.setInstanceSpec(group.getInstanceSpec());
            groupStatusVO.setEnvironment(group.getEnvironment());
            groupStatusVO.setAppId(group.getAppId());

            ApplicationEntity applicationEntity = appService.getAppByCmdbId(group.getAppId());
            groupStatusVO.setAppName(applicationEntity.getName());
            groupStatusVO.setDomain(appService.getAppDomainByEnv(applicationEntity, group.getEnvironment()));
            groupStatusVO.setOwner(applicationEntity.getOwner());

            Boolean bool = resourceService.ifAppEnableStaticResource(groupStatusVO.getAppId(), groupStatusVO.getEnvironment());
            groupStatusVO.setEnableStaticResource(bool);

            List<String> zones = zoneService.fetchZoneNamesByEnv(groupStatusVO.getEnvironment());
            groupStatusVO.setZones(zones);
        }

        return groupStatusVO;
    }

    /**
     * 获取指定站点的下一个发布组名，名字格式为：站点ID+发布组编号，其中发布组编号为当前发布组个数增1
     *
     * @return 返回规范的下一个发布组名
     */
    private String formatNextSiteGroupName(String appId) {
        List<GroupEntity> groupList = groupRepo.findByAppIdEx(appId);
        Integer countOfGroup = groupList.size();

        // check if there is existing group name
        // if yes, append timestamp at the end of group name
        String newGroupName = String.format("a%s.g%s", appId, countOfGroup + 1);
        GroupEntity groupEntity = groupRepo.findGroupByNameEx(newGroupName);
        if (groupEntity != null) {
            newGroupName = String.format("%s.%s", newGroupName, System.currentTimeMillis());
        }

        return newGroupName;
    }

    /**
     * 获取指定站点发布组信息
     *
     * @param groupId 指定的发布组ID
     * @return 返回站点发布组实体信息
     */
    public GroupEntity getGroupById(Long groupId) {
        return groupRepo.findOne(groupId);
    }

    public Boolean deleteGroup(Long groupId) {
        int count = groupRepo.removeById(groupId);
        return count > 0;
    }

    /**
     * 获取最近使用过的镜像列表
     * @return
     */
    public List<GroupEntity> findRecentUsedImages(String env, String appId) {
        Pageable pageable = new PageRequest(0, 5);
        return groupRepo.findRecentUsedImages(env, appId, pageable);
    }

    /**
     * 根据环境获取所有发布组
     * @param environment
     * @return
     */
    public List<GroupEntity> findByEnvironment(String environment) {
        return groupRepo.findByEnvironment(environment);
    }

    public List<GroupEntity> findAllGroups() {
        return groupRepo.findAll();
    }
}
