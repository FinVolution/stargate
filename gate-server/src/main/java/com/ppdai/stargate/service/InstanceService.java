package com.ppdai.stargate.service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.ppdai.atlas.client.model.ZoneDto;
import com.ppdai.stargate.constant.ConstantValue;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.manager.TaskManager;
import com.ppdai.stargate.po.*;
import com.ppdai.stargate.remote.RemoteRegistry;
import com.ppdai.stargate.remote.RemoteRegistryManager;
import com.ppdai.stargate.utils.ConvertUtil;
import com.ppdai.stargate.utils.NamingUtil;
import com.ppdai.stargate.vo.*;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodCondition;
import io.kubernetes.client.models.V1PodList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ppdai.stargate.dao.InstanceRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;

@Service
@Slf4j
public class InstanceService {

    @Autowired
    private InstanceRepository instanceRepo;
    @Autowired
    private RemoteRegistryManager remoteRegistryManager;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ContainerService containerService;
    @Autowired
    private ZoneService zoneService;
    @Autowired
    private AppService appService;
    @Autowired
    private InstanceJobService instanceJobService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private Environment environment;

    private static String RESTART_SUFFIX = "-restart";

    private ExecutorService parallelQueryExecutor = Executors.newFixedThreadPool(50);

    public List<InstanceEntity> getInstancesByGroupId(Long groupId) {
        return instanceRepo.findInstancesByGroupId(groupId);
    }

    public List<InstanceEntity> getInstancesByGroupIdEx(Long groupId) {
        return instanceRepo.findInstancesByGroupIdEx(groupId);
    }

    public InstanceEntity saveInstance(InstanceEntity instanceEntity) {
        return instanceRepo.saveAndFlush(instanceEntity);
    }

    /**
     * 获取机器实例的名字，名字格式为：应用ID + 发布组名 + 实例index，其中实例index需要增1
     * 该命名规范保证了实例名字的唯一性，并和PAAS实例命名保持一致
     *
     * @param groupName 应用发布组ID
     * @param index     实例在应用发布组中的index
     * @return 返回规范的机器实例名字
     */
    public String formatInstanceName(String groupName, Integer index) {
        String name = String.format("stargate.%s.%s", groupName, index + 1);
        // 实例名会变成k8s里面的pod名称, k8s里面pod的名称要求不超过63个字符
        if (name.length() > 63) {
            name = name.substring(name.length() - 63);
            if (name.startsWith(".")) {
                name = name.substring(1);
            }
        }

        return name;
    }

    public InstanceEntity findByName(String name) {
        return instanceRepo.findByName(name);
    }

    public List<InstanceEntity> findByEnv(String env) {
        return instanceRepo.findByEnv(env);
    }

    public InstanceEntity findByNameEx(String name) {
        return instanceRepo.findByNameEx(name);
    }

    public InstanceEntity findByIp(String ip) {
        return instanceRepo.findInstanceByIp(ip);
    }

    public Integer findInstanceCountByGroupIdAndZone(Long groupId, String zone) {
        return instanceRepo.countByGroupIdAndZone(groupId, zone);
    }

    /**
     * 从实例名获取序号
     * @param name
     * @return
     */
    public int exractSeqFromInstanceName(String name) {
        String[] fields = name.split("\\.");
        return Integer.parseInt(fields[fields.length - 1]);
    }

    public long getActiveInstanceCountByGroup(Long groupId) {
        long count = instanceRepo.count((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            list.add(criteriaBuilder.equal(root.get("groupId").as(Long.class), groupId));
            list.add(criteriaBuilder.equal(root.get("isActive").as(Boolean.class), Boolean.TRUE));

            Predicate[] p = new Predicate[list.size()];
            return criteriaBuilder.and(list.toArray(p));
        });
        return count;
    }

    public List<Object[]> findRecentUsedImages(String appId, String env) {
        return instanceRepo.findRecentUsedImages(appId, env, new PageRequest(0,100));
    }

    public InstanceEntity findById(Long id) {
        return instanceRepo.findOne(id);
    }

    public List<InstanceEntity> findAllInstances() {
        return instanceRepo.findAllInstances();
    }

    public List<K8sInstanceVO> findAllK8sInstances() throws ApiException {
        List<K8sInstanceVO> k8sInstanceVOList = new ArrayList<>();

        List<String> k8sList = new ArrayList<>();
        List<ZoneDto> zoneDtos = zoneService.fetchAllZones();

        for (ZoneDto zoneDto : zoneDtos) {
            if (k8sList.contains(zoneDto.getK8s())) {
                continue;
            }
            k8sList.add(zoneDto.getK8s());

            V1PodList v1PodList = containerService.getContainersByZone(zoneDto.getEnvName(), zoneDto.getName());
            for (V1Pod v1Pod : v1PodList.getItems()) {
                K8sInstanceVO k8sInstanceVO = new K8sInstanceVO();
                k8sInstanceVO.setK8s(zoneDto.getK8s());
                k8sInstanceVO.setHostIp(v1Pod.getStatus().getHostIP());
                k8sInstanceVO.setIp(v1Pod.getStatus().getPodIP());
                k8sInstanceVO.setName(v1Pod.getMetadata().getName());
                k8sInstanceVOList.add(k8sInstanceVO);
            }
        }

        return k8sInstanceVOList;
    }

    public PageVO<InstanceEntity> findCloudInstancesByCondition(String env, String appId, String name, String ip, Integer page, Integer size) {
        PageVO<InstanceEntity> instancePageVO = new PageVO<>();
        Pageable pageable = new PageRequest(page - 1, size);

        Page<InstanceEntity> instancePage = instanceRepo.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (!StringUtils.isEmpty(env)) {
                list.add(criteriaBuilder.equal(root.get("env").as(String.class), env));
            }
            if (!StringUtils.isEmpty(appId)) {
                list.add(criteriaBuilder.equal(root.get("appId").as(String.class), appId));
            }
            if (!StringUtils.isEmpty(name)) {
                list.add(criteriaBuilder.equal(root.get("name").as(String.class), name));
            }
            if (!StringUtils.isEmpty(ip)) {
                list.add(criteriaBuilder.equal(root.get("slotIp").as(String.class), ip));
            }
            list.add(criteriaBuilder.equal(root.get("groupId").as(Long.class), 0L));
            list.add(criteriaBuilder.equal(root.get("isActive").as(Boolean.class), Boolean.TRUE));
            Predicate[] p = new Predicate[list.size()];
            return criteriaBuilder.and(list.toArray(p));
        }, pageable);

        instancePageVO.setContent(instancePage.getContent());
        instancePageVO.setTotalElements(instancePage.getTotalElements());

        return instancePageVO;
    }

    public List<InstanceEntity> findGroupInstancesByEnv(String env) {
        return instanceRepo.findGroupInstancesByEnv(env);
    }

    public List<InstanceEntity> findInstancesByHostIp(String env, String hostIp) {
        List<InstanceEntity> instances = new ArrayList<>();

        V1PodList v1PodList;
        try {
            v1PodList = containerService.getContainersByHostIp(env, hostIp);
        } catch (ApiException e) {
            log.error("查询Pod失败, err=" + e.getMessage(), e);
            throw BaseException.newException(MessageType.ERROR, "查询Pod失败, err=" + e.getMessage());
        }

        List<InstanceEntity> allInstances = instanceRepo.findAllInstances();

        if (v1PodList.getItems().size() > 0) {
            for (V1Pod v1Pod : v1PodList.getItems()) {
                Optional<InstanceEntity> optional = allInstances.stream().filter(x -> x.getName().equals(v1Pod.getMetadata().getName())).findFirst();
                if (optional.isPresent()) {
                    instances.add(optional.get());
                }
            }
        }

        return instances;
    }

    public List<InstanceEntity> findByEnvAndAppId(String env, String appId) {
        return instanceRepo.findInstancesByEnvAndAppId(env, appId);
    }

    public List<InstanceEntity> findByAppId(String appId) {
        return instanceRepo.findInstancesByAppId(appId);
    }

    public void checkInstanceQuota(InstanceEntity instanceEntity) {
        groupService.checkSiteQuota(instanceEntity.getAppId(), instanceEntity.getEnv(), 1, instanceEntity.getSpec());
    }

    @Transactional
    public InstanceEntity setResourceOverSubscribeFlag(InstanceEntity instanceEntity) {
        /**
         * 对实例设置是否要进行内存资源的超售
         * 目前对同一应用下的50%的实例进行超售
         */
        List<InstanceEntity> instances = findByEnvAndAppId(instanceEntity.getEnv(), instanceEntity.getAppId());
        for (int i = 0; i < instances.size(); i++) {
            if (instances.get(i).getName().equals(instanceEntity.getName())) {
                if ((i % 2) == 0) {
                    instanceEntity.setFlags(instanceEntity.getFlags() & (~ConstantValue.MEM_OVERSUBSCRIBE_FLAG));
                } else {
                    instanceEntity.setFlags(instanceEntity.getFlags() | ConstantValue.MEM_OVERSUBSCRIBE_FLAG);
                }
            }
        }

        return saveInstance(instanceEntity);
    }

    @Transactional
    public InstanceEntity allotZoneForInstance(InstanceEntity instanceEntity) {
        List<ZoneInstanceCountVO> zoneInstanceCountVOList = new ArrayList<>();

        List<String> zones = zoneService.fetchZoneNamesByEnv(instanceEntity.getEnv());

        for (int i = 0; i < zones.size(); i++) {
            ZoneInstanceCountVO zoneInstanceCountVO = new ZoneInstanceCountVO();
            zoneInstanceCountVO.setZone(zones.get(i));
            Integer usedCount = instanceRepo.countByEnvAndAppIdAndZone(instanceEntity.getEnv(), instanceEntity.getAppId(), zones.get(i));
            zoneInstanceCountVO.setUsedCount(usedCount);
            zoneInstanceCountVOList.add(zoneInstanceCountVO);
        }

        if (zoneInstanceCountVOList.isEmpty()) {
            throw BaseException.newException(MessageType.ERROR, "没有可调度的集群, env = %s", instanceEntity.getEnv());
        }

        // zone按已有实例数升序排列
        zoneInstanceCountVOList.sort(Comparator.comparingInt(ZoneInstanceCountVO::getUsedCount));
        instanceEntity.setZone(zoneInstanceCountVOList.get(0).getZone());

        return saveInstance(instanceEntity);
    }

    public InstanceV2VO getInstanceStatus(InstanceEntity instanceEntity) {

        ApplicationEntity applicationEntity = appService.getAppByCmdbId(instanceEntity.getAppId());

        String domain = appService.getAppDomainByEnv(applicationEntity, instanceEntity.getEnv());

        String appId = instanceEntity.getAppId();
        String appName = instanceEntity.getAppName();
        String env = instanceEntity.getEnv();
        String instanceName = instanceEntity.getName();
        String zone = instanceEntity.getZone();

        RemoteRegistry remoteRegistry = remoteRegistryManager.getRemoteRegistryByDomain(domain);
        InstanceV2VO instanceV2VO = remoteRegistry.getInstanceStatus(domain, env, appId, appName, instanceEntity);

        instanceV2VO.setAppName(appName);
        instanceV2VO.setSwitchBitset(instanceEntity.getFlags());

        // 查询资源表，检查实例是否为静态资源
        ResourceEntity resourceEntity = resourceService.findByPodName(instanceName);
        if (resourceEntity != null) {
            instanceV2VO.setIsStatic(resourceEntity.getIsStatic());
        }

        V1Pod v1Pod = null;
        V1PodList v1PodList;

        try {
            v1PodList = containerService.getContainersByInstanceName(env, instanceEntity.getNamespace(), instanceName, zone);
        } catch (ApiException e) {
            log.error("查询Pod失败, err=" + e.getMessage(), e);
            throw BaseException.newException(MessageType.ERROR, "查询Pod失败, err=" + e.getMessage());
        }

        Optional<V1Pod> podOpt = v1PodList.getItems()
                .stream()
                .filter(x -> x.getMetadata().getName().equals(instanceName))
                .findFirst();

        if (podOpt.isPresent()) {
            v1Pod = podOpt.get();
        }

        if (v1Pod != null && v1Pod.getStatus() != null) {
            instanceV2VO.setInstanceIp(v1Pod.getStatus().getPodIP());
            instanceV2VO.setHostIp(v1Pod.getStatus().getHostIP());
            instanceV2VO.setContainerStatus(v1Pod.getStatus().getPhase());

            if (v1Pod.getStatus().getContainerStatuses() != null && v1Pod.getStatus().getContainerStatuses().size() > 0) {
                String containerID = v1Pod.getStatus().getContainerStatuses().get(0).getContainerID();
                if (containerID != null) {
                    instanceV2VO.setContainerFullId(containerID.substring(9));
                    instanceV2VO.setContainerId(instanceV2VO.getContainerFullId().substring(0, 12));
                }

                //添加容器终端地址
                String k8sDashboardServer = zoneService.fetchDashboardByZoneAndEnv(zone, env);
                if (!StringUtils.isEmpty(k8sDashboardServer)) {
                    String containerName = v1Pod.getStatus().getContainerStatuses().get(0).getName();
                    StringBuilder containerConsoleUrl = new StringBuilder();
                    containerConsoleUrl.append(k8sDashboardServer).append("/#/shell/").append(instanceEntity.getNamespace()).append("/").append(instanceName).append("/").append(containerName).append("?namespace=_all");
                    instanceV2VO.setContainerConsoleUrl(containerConsoleUrl.toString());
                }
            }

            // 实例刚发布完的一段时间内ready状态直接设置为false，防止用户立刻拉入流量
            Boolean ready = false;
            long runTime = System.currentTimeMillis() - instanceV2VO.getReleaseTime().getTime();
            long delayTime = Long.parseLong(environment.getProperty("stargate.pullIn.initialDelay", "30000"));
            if (runTime > delayTime && v1Pod.getStatus().getConditions() != null) {
                Optional<V1PodCondition> v1PodCondition = v1Pod.getStatus().getConditions()
                        .stream()
                        .filter(x -> x.getType().equals("Ready"))
                        .findFirst();

                if (v1PodCondition.isPresent() && v1PodCondition.get().getStatus().equals("True")) {
                    ready = true;
                }
            }
            instanceV2VO.setReady(ready);
        }

        return instanceV2VO;
    }

    public List<InstanceVO> getInstanceStatusByGroupId(Long groupId) {
        List<InstanceVO> instanceVOList = new ArrayList<>();

        GroupEntity groupEntity = groupService.getGroupById(groupId);
        if (groupEntity == null) {
            throw BaseException.newException(MessageType.ERROR, "发布组不存在, groupId=" + groupId);
        }

        ApplicationEntity applicationEntity = appService.getAppByCmdbId(groupEntity.getAppId());
        if (applicationEntity == null) {
            throw BaseException.newException(MessageType.ERROR, "应用不存在, appId=" + groupEntity.getAppId());
        }

        List<InstanceEntity> instanceEntityList = getInstancesByGroupId(groupId);

        CountDownLatch waitAllQuery = new CountDownLatch(instanceEntityList.size());

        // 并发查询
        for (InstanceEntity instanceEntity : instanceEntityList) {
            try {
                parallelQueryExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InstanceV2VO instanceV2VO = getInstanceStatus(instanceEntity);
                            InstanceVO instanceVO = ConvertUtil.convert(instanceV2VO, InstanceVO.class);

                            int seq = exractSeqFromInstanceName(instanceVO.getName());
                            instanceVO.setId((long) seq);

                            instanceVO.setGroupId(groupId);
                            instanceVO.setGroupName(groupEntity.getName());
                            instanceVO.setDepartment(applicationEntity.getDepartmentCode());
                            instanceVO.setSlbSiteServerId(instanceEntity.getFlags());
                            instanceVO.setIp(instanceV2VO.getInstanceIp());
                            instanceVO.setPort(instanceV2VO.getPort().toString());
                            instanceVO.setInstanceSpec(instanceV2VO.getSpec());
                            instanceVO.setAgentHost(instanceV2VO.getHostIp());

                            if (instanceV2VO.getOpsPulledIn() != null) {
                                instanceVO.setOpsPulledIn(instanceV2VO.getOpsPulledIn() == true ? 1 : 0);
                            }

                            // 添加实例表或发布组中的image信息，去掉重启后缀
                            String image = instanceEntity.getImage();
                            if (image.endsWith(RESTART_SUFFIX)) {
                                image = image.substring(0, image.length() - RESTART_SUFFIX.length());
                            }
                            instanceVO.setReleaseTarget(image);
                            String[] splits = image.split(":");
                            instanceVO.setReleaseVersion(splits[1]);

                            synchronized (instanceVOList) {
                                instanceVOList.add(instanceVO);
                            }
                        } catch (Throwable throwable) {
                            log.error("查询发布组实例失败, instance=" + instanceEntity.getName() + ", err=" + throwable.getMessage(), throwable);
                        } finally {
                            waitAllQuery.countDown();
                        }
                    }
                });
            } catch (Throwable t) {
                waitAllQuery.countDown();
                log.error("添加查询发布组实例任务失败, instance=" + instanceEntity.getName() + ", err=" + t.getMessage(), t);
            }
        }

        try {
            waitAllQuery.await();
        } catch (InterruptedException e) {
        }

        return instanceVOList;
    }

    public List<InstanceVO> getInstanceStatusByAppIdAndEnv(String appId, String env) {
        List<InstanceVO> instanceVOList = new ArrayList<>();

        List<InstanceEntity> instanceEntityList = findByEnvAndAppId(env, appId);

        CountDownLatch waitAllQuery = new CountDownLatch(instanceEntityList.size());

        // 并发查询
        for (InstanceEntity instanceEntity : instanceEntityList) {
            try {
                parallelQueryExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InstanceV2VO instanceV2VO = getInstanceStatus(instanceEntity);
                            InstanceVO instanceVO = ConvertUtil.convert(instanceV2VO, InstanceVO.class);

                            if (!instanceEntity.getGroupId().equals(0L)) {
                                GroupEntity groupEntity = groupService.getGroupById(instanceEntity.getGroupId());
                                instanceVO.setGroupName(groupEntity.getName());
                            }

                            instanceVO.setSlbSiteServerId(instanceEntity.getFlags());
                            instanceVO.setIp(instanceEntity.getSlotIp());
                            instanceVO.setPort(instanceEntity.getPort().toString());
                            instanceVO.setInstanceSpec(instanceEntity.getSpec());
                            instanceVO.setAgentHost(instanceV2VO.getHostIp());

                            if (instanceV2VO.getOpsPulledIn() != null) {
                                instanceVO.setOpsPulledIn(instanceV2VO.getOpsPulledIn() == true ? 1 : 0);
                            }

                            // 添加实例表中的image信息，去掉重启后缀
                            String image = instanceEntity.getImage();
                            if (image.endsWith(RESTART_SUFFIX)) {
                                image = image.substring(0, image.length() - RESTART_SUFFIX.length());
                            }
                            instanceVO.setReleaseTarget(image);
                            String[] splits = image.split(":");
                            instanceVO.setReleaseVersion(splits[1]);

                            synchronized (instanceVOList) {
                                instanceVOList.add(instanceVO);
                            }
                        } catch (Throwable throwable) {
                            log.error("查询应用实例失败, instance=" + instanceEntity.getName() + ", err=" + throwable.getMessage(), throwable);
                        } finally {
                            waitAllQuery.countDown();
                        }
                    }
                });
            } catch (Throwable t) {
                waitAllQuery.countDown();
                log.error("添加查询应用实例任务失败, instance=" + instanceEntity.getName() + ", err=" + t.getMessage(), t);
            }
        }

        try {
            waitAllQuery.await();
        } catch (InterruptedException e) {
        }

        return instanceVOList;
    }

    public List<InstanceVO> getInstanceStatusByIp(String ip) {
        List<InstanceVO> instanceVOList = new ArrayList<>();

        InstanceEntity instanceEntity = findByIp(ip);

        if (instanceEntity != null) {
            InstanceV2VO instanceV2VO = getInstanceStatus(instanceEntity);
            InstanceVO instanceVO = ConvertUtil.convert(instanceV2VO, InstanceVO.class);

            if (!instanceEntity.getGroupId().equals(0L)) {
                GroupEntity groupEntity = groupService.getGroupById(instanceEntity.getGroupId());
                instanceVO.setGroupName(groupEntity.getName());
            }

            instanceVO.setSlbSiteServerId(instanceEntity.getFlags());
            instanceVO.setIp(instanceEntity.getSlotIp());
            instanceVO.setPort(instanceEntity.getPort().toString());
            instanceVO.setInstanceSpec(instanceEntity.getSpec());
            instanceVO.setAgentHost(instanceV2VO.getHostIp());

            if (instanceV2VO.getOpsPulledIn() != null) {
                instanceVO.setOpsPulledIn(instanceV2VO.getOpsPulledIn() == true ? 1 : 0);
            }

            // 添加实例表中的image信息，去掉重启后缀
            String image = instanceEntity.getImage();
            if (image.endsWith(RESTART_SUFFIX)) {
                image = image.substring(0, image.length() - RESTART_SUFFIX.length());
            }
            instanceVO.setReleaseTarget(image);
            String[] splits = image.split(":");
            instanceVO.setReleaseVersion(splits[1]);

            instanceVOList.add(instanceVO);
        }

        return instanceVOList;
    }

    public void transferInstances(TransferInstanceVO transferInstanceVO) {
        String env = transferInstanceVO.getEnv();
        String hostIp = transferInstanceVO.getHostIp();
        List<Long> instanceIds = transferInstanceVO.getInstanceIds();

        V1PodList v1PodList;
        try {
            v1PodList = containerService.getContainersByHostIp(env, hostIp);
        } catch (ApiException e) {
            log.error("查询Pod失败, err=" + e.getMessage(), e);
            throw BaseException.newException(MessageType.ERROR, "查询Pod失败, err=" + e.getMessage());
        }

        if (v1PodList.getItems().isEmpty()) {
            return;
        }

        for (int i = 0; i < instanceIds.size(); i++) {
            InstanceEntity instanceEntity = instanceRepo.findOne(instanceIds.get(i));
            Optional<V1Pod> optional = v1PodList.getItems().stream().filter(x -> instanceEntity.getName().equals(x.getMetadata().getName())).findFirst();
            if (!optional.isPresent()) {
                continue;
            }

            // 实例有任务在执行中则不迁移
            if (taskManager.hasInProcessTasksByInstance(instanceEntity.getId())) {
                continue;
            }

            Map<String, String> dataMap = new HashMap<>();
            dataMap.put(NamingUtil.NOT_READY_FLOW, instanceEntity.getHasPulledIn().toString());

            // 创建configmap
            try {
                containerService.createConfigMap(instanceEntity.getEnv(), instanceEntity.getZone(), instanceEntity.getNamespace(), instanceEntity.getName(), dataMap);
            } catch (ApiException e) {
                log.error("创建configmap失败, err=" + e.getMessage(), e);
                throw BaseException.newException(MessageType.ERROR, "创建configmap失败, err=" + e.getMessage());
            }

            // 添加迁移实例任务
            instanceJobService.recoverInstance1Step(instanceEntity);
        }
    }

    public List<EnvInstanceVO> getInstancesByEnv(String env) {
        List<EnvInstanceVO> envInstanceVOList = new ArrayList<>();

        List<InstanceEntity> instanceEntityList = findByEnv(env);
        for (int i = 0; i < instanceEntityList.size(); i++) {
            InstanceEntity instanceEntity = instanceEntityList.get(i);
            EnvInstanceVO envInstanceVO = new EnvInstanceVO();
            envInstanceVO.setName(instanceEntity.getName());
            envInstanceVO.setEnv(env);
            envInstanceVO.setAppId(instanceEntity.getAppId());
            envInstanceVO.setSpec(instanceEntity.getSpec());
            envInstanceVO.setImage(instanceEntity.getImage());
            envInstanceVO.setIp(instanceEntity.getSlotIp());
            envInstanceVO.setPort(instanceEntity.getPort());
            envInstanceVO.setHasPulledIn(instanceEntity.getHasPulledIn());
            envInstanceVO.setReleaseTime(instanceEntity.getReleaseTime());

            ApplicationEntity applicationEntity = appService.getAppByCmdbId(instanceEntity.getAppId());
            envInstanceVO.setAppName(applicationEntity.getName());
            envInstanceVO.setDepartment(applicationEntity.getDepartment());

            envInstanceVOList.add(envInstanceVO);
        }

        return envInstanceVOList;
    }

    public List<InstanceCountVO> getInstanceCountByZone() {
        List<InstanceCountVO> instanceCountVOList = new ArrayList<>();

        List<ZoneDto> zoneDtos = zoneService.fetchAllZones();
        for (int i = 0; i < zoneDtos.size(); i++) {
            String env = zoneDtos.get(i).getEnvName();
            String zone = zoneDtos.get(i).getName();

            InstanceCountVO instanceCountVO = new InstanceCountVO();
            instanceCountVO.setEnv(env);
            instanceCountVO.setZone(zone);

            List<ResourceEntity> resources = resourceService.findResourcesByEnvAndZone(env, zone);
            if ("fat".equals(env)) {
                // 部分多环境实例虽然占用资源但未发布
                List<ResourceEntity> multiEnvResources = resources.stream().filter(x -> x.getPodName().startsWith("env-")).collect(Collectors.toList());
                List<InstanceEntity> cloudInstances = instanceRepo.findCloudInstancesByEnvAndZone(env, zone);
                List<InstanceEntity> multiEnvInstances = cloudInstances.stream().filter(x -> x.getName().startsWith("env-")).collect(Collectors.toList());
                instanceCountVO.setInstanceCount(resources.size() - multiEnvResources.size() + multiEnvInstances.size());
            } else {
                instanceCountVO.setInstanceCount(resources.size());
            }

            instanceCountVOList.add(instanceCountVO);
        }

        return instanceCountVOList;
    }
}
