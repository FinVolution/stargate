package com.ppdai.stargate.service;

import com.ppdai.atlas.client.model.AppQuotaDto;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.dao.InstanceRepository;
import com.ppdai.stargate.dao.IpRepository;
import com.ppdai.stargate.dao.ResourceRepository;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.po.*;
import com.ppdai.stargate.remote.RemoteCmdb;
import com.ppdai.stargate.vo.PageVO;
import com.ppdai.stargate.vo.ResourceQuotaStatusVO;
import com.ppdai.stargate.vo.ZoneResourceCountVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {
    @Autowired
    private ResourceRepository resourceRepo;

    @Autowired
    private IpRepository ipRepo;

    @Autowired
    private InstanceRepository instanceRepo;

    @Autowired
    private IpService ipService;

    @Autowired
    private AppService appService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private RemoteCmdb remoteCmdb;

    public String findIpByPodName(String podName) {
        ResourceEntity resourceEntity = resourceRepo.findByPodName(podName);
        if (resourceEntity == null) {
            throw BaseException.newException(MessageType.ERROR, "资源不存在, podName = %s", podName);
        }
        return resourceEntity.getIp();
    }

    public ResourceEntity findByPodName(String podName) {
        return resourceRepo.findByPodName(podName);
    }

    public ResourceEntity findByIp(String appId, String env, String spec, String ip) {
        return resourceRepo.findStaticResourceByIp(appId, env, spec, ip);
    }

    public List<ResourceEntity> findByAppIdAndEnv(String appId, String env) {
        return resourceRepo.findByAppIdAndEnv(appId, env);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addStaticResource(String appId, String env, String spec, Integer number, String zone) {
        ApplicationEntity applicationEntity = appService.getAppByCmdbId(appId);
        String appName = applicationEntity.getName();

        List<ResourceQuotaStatusVO> resourceQuotaStatusVOS = fetchResourceQuotaStatus(appId, env);
        Optional<ResourceQuotaStatusVO> resourceQuotaStatusVO = resourceQuotaStatusVOS.stream().filter(x -> x.getSpectypeName().equals(spec)).findFirst();
        if (!resourceQuotaStatusVO.isPresent()) {
            throw BaseException.newException(MessageType.ERROR, "获取配额失败, 请检查应用是否创建了该规格的配额");
        } else if (number > resourceQuotaStatusVO.get().getFreeCount()) {
            throw BaseException.newException(MessageType.ERROR, "资源配额不足, 应用: %s, 环境: %s, 规格: %s, 总额: %d, 已使用: %d, 申请数: %d",
                    appName, env, spec, resourceQuotaStatusVO.get().getTotal(), resourceQuotaStatusVO.get().getUsedCount(), number);
        }

        List<ResourceEntity> resourceEntityList = new ArrayList<>();

        // 如果zone不为空则直接分配资源，否则为每个zone均匀分配资源
        if (StringUtils.isNotEmpty(zone)) {
            String network = zoneService.fetchNetworkByZoneAndEnv(zone, env);
            List<IpEntity> unoccupiedIpList = ipService.findUnoccupiedIp(network);
            if (unoccupiedIpList.size() < number) {
                throw BaseException.newException(MessageType.ERROR, "可用ip不足, network = %s", network);
            }

            for (int i = 0; i < unoccupiedIpList.size(); i++) {
                IpEntity ipEntity = unoccupiedIpList.get(i);
                int n = ipRepo.occupy(ipEntity.getIp());
                if (n > 0) {
                    ResourceEntity resourceEntity = new ResourceEntity();
                    resourceEntity.setAppId(appId);
                    resourceEntity.setAppName(appName);
                    resourceEntity.setEnv(env);
                    resourceEntity.setZone(zone);
                    resourceEntity.setIp(ipEntity.getIp());
                    resourceEntity.setSpec(spec);
                    resourceEntity.setIsStatic(true);
                    resourceEntityList.add(resourceEntity);
                }
                if (resourceEntityList.size() == number) {
                    break;
                }
            }
        } else {
            List<ZoneResourceCountVO> zoneResourceCountVOList = new ArrayList<>();

            List<String> zones = zoneService.fetchZoneNamesByEnv(env);

            for (int i = 0; i < zones.size(); i++) {
                List<ResourceEntity> resources = resourceRepo.findAvailableStaticResourcesByZone(appId, env, spec, zones.get(i));
                ZoneResourceCountVO zoneResourceCountVO = new ZoneResourceCountVO();
                zoneResourceCountVO.setZone(zones.get(i));
                zoneResourceCountVO.setUsedCount(resources.size());
                zoneResourceCountVO.setAllotCount(0);
                zoneResourceCountVOList.add(zoneResourceCountVO);
            }

            if (zoneResourceCountVOList.isEmpty()) {
                throw BaseException.newException(MessageType.ERROR, "没有可调度的集群, env = %s", env);
            }

            // zone按已有资源数升序排列
            zoneResourceCountVOList.sort(Comparator.comparingInt(ZoneResourceCountVO::getUsedCount));

            // 遍历zone，一次分配一个资源，直到分完
            while (number > 0) {
                for (ZoneResourceCountVO zoneResourceCountVO : zoneResourceCountVOList) {
                    zoneResourceCountVO.setAllotCount(zoneResourceCountVO.getAllotCount() + 1);

                    number--;
                    if (number <= 0) {
                        break;
                    }
                }
            }

            for (int i = 0; i < zoneResourceCountVOList.size(); i++) {
                ZoneResourceCountVO zoneResourceCountVO = zoneResourceCountVOList.get(i);

                // 若为zone分配的资源数为0，则跳过
                if (zoneResourceCountVO.getAllotCount() == 0) {
                    continue;
                }

                String network = zoneService.fetchNetworkByZoneAndEnv(zoneResourceCountVO.getZone(), env);
                List<IpEntity> unoccupiedIpList = ipService.findUnoccupiedIp(network);
                if (unoccupiedIpList.size() < zoneResourceCountVO.getAllotCount()) {
                    throw BaseException.newException(MessageType.ERROR, "可用ip不足, network = %s", network);
                }

                List<ResourceEntity> zoneResources = new ArrayList<>();
                for (int j = 0; j < unoccupiedIpList.size(); j++) {
                    IpEntity ipEntity = unoccupiedIpList.get(j);
                    int n = ipRepo.occupy(ipEntity.getIp());
                    if (n > 0) {
                        ResourceEntity resourceEntity = new ResourceEntity();
                        resourceEntity.setAppId(appId);
                        resourceEntity.setAppName(appName);
                        resourceEntity.setEnv(env);
                        resourceEntity.setZone(zoneResourceCountVO.getZone());
                        resourceEntity.setIp(ipEntity.getIp());
                        resourceEntity.setSpec(spec);
                        resourceEntity.setIsStatic(true);
                        zoneResources.add(resourceEntity);
                    }
                    if (zoneResources.size() == zoneResourceCountVO.getAllotCount()) {
                        break;
                    }
                }

                resourceEntityList.addAll(zoneResources);
            }
        }

        resourceRepo.save(resourceEntityList);
        resourceRepo.flush();
    }

    public PageVO<ResourceEntity> findResourcesByPage(String appId, String env, String spec, String ip, Boolean isStatic, Integer page, Integer size) {
        PageVO<ResourceEntity> resourcePageVO = new PageVO<>();

        Pageable pageable = new PageRequest(page - 1, size);

        Page<ResourceEntity> resourcePage = resourceRepo.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();

            if (StringUtils.isNotEmpty(appId)) {
                list.add(criteriaBuilder.equal(root.get("appId").as(String.class), appId));
            }

            if (StringUtils.isNotEmpty(env)) {
                list.add(criteriaBuilder.equal(root.get("env").as(String.class), env));
            }

            if (StringUtils.isNotEmpty(spec)) {
                list.add(criteriaBuilder.equal(root.get("spec").as(String.class), spec));
            }

            if (StringUtils.isNotEmpty(ip)) {
                list.add(criteriaBuilder.equal(root.get("ip").as(String.class), ip));
            }

            if (isStatic != null) {
                list.add(criteriaBuilder.equal(root.get("isStatic").as(Boolean.class), isStatic));
            }

            list.add(criteriaBuilder.equal(root.get("isActive").as(Boolean.class), Boolean.TRUE));

            Predicate[] p = new Predicate[list.size()];
            return criteriaBuilder.and(list.toArray(p));
        }, pageable);

        resourcePageVO.setContent(resourcePage.getContent());
        resourcePageVO.setTotalElements(resourcePage.getTotalElements());

        return resourcePageVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeResourceById(Long id) {
        ResourceEntity resourceEntity = resourceRepo.findById(id);
        if (resourceEntity == null) {
            throw BaseException.newException(MessageType.ERROR, "资源[id=%d]不存在", id);
        }
        if (resourceEntity.getPodName() != null) {
            throw BaseException.newException(MessageType.ERROR, "资源[ip=%s]已被实例占用", resourceEntity.getIp());
        }
        resourceEntity.setIsActive(false);
        resourceRepo.saveAndFlush(resourceEntity);

        IpEntity ipEntity = ipRepo.findByIp(resourceEntity.getIp());
        if (ipEntity != null) {
            ipEntity.setOccupied(false);
            ipRepo.saveAndFlush(ipEntity);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResourceEntity createResourceByInstance(String appId, String appName, String env, String spec, String podName, String zone, Boolean isStatic) {
        ResourceEntity newResource = null;

        ResourceEntity resource = resourceRepo.findByPodName(podName);
        if (resource == null) {
            List<ResourceEntity> availableStaticResources = resourceRepo.findAvailableStaticResourcesByZone(appId, env, spec, zone);
            if (!availableStaticResources.isEmpty() && isStatic) {
                for (int i = 0; i < availableStaticResources.size(); i++) {
                    ResourceEntity resourceEntity = availableStaticResources.get(i);
                    int count = resourceRepo.setPodNameForStaticResource(resourceEntity.getId(), podName);
                    if (count > 0) {
                        resourceEntity.setPodName(podName);
                        newResource = resourceEntity;
                        break;
                    }
                }
            } else {
                String network = zoneService.fetchNetworkByZoneAndEnv(zone, env);

                List<IpEntity> unoccupiedIpList = ipService.findUnoccupiedIp(network);
                if (unoccupiedIpList.isEmpty()) {
                    throw BaseException.newException(MessageType.ERROR, "可用IP不足, network = %s", network);
                }

                for (int i = 0; i < unoccupiedIpList.size(); i++) {
                    IpEntity ipEntity = unoccupiedIpList.get(i);
                    int n = ipRepo.occupy(ipEntity.getIp());
                    if (n > 0) {
                        ResourceEntity resourceEntity = new ResourceEntity();
                        resourceEntity.setAppId(appId);
                        resourceEntity.setAppName(appName);
                        resourceEntity.setEnv(env);
                        resourceEntity.setZone(zone);
                        resourceEntity.setSpec(spec);
                        resourceEntity.setIp(ipEntity.getIp());
                        resourceEntity.setPodName(podName);
                        resourceEntity.setIsStatic(isStatic);
                        resourceRepo.saveAndFlush(resourceEntity);
                        newResource = resourceEntity;
                        break;
                    }
                }
            }
        } else {
            newResource = resource;
        }

        if (newResource == null) {
            throw BaseException.newException(MessageType.ERROR, "根据实例创建资源失败, podName = %s", podName);
        }

        return newResource;
    }

    public void releaseResourceByInstance(String podName) {
        InstanceEntity instanceEntity = instanceRepo.findByName(podName);
        if (instanceEntity == null) {
            removePodNameFromResource(podName);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String setPodNameForDynamicResource(String appId, String appName, String env, String spec, String podName, String zone) {
        String resourceIp = null;

        ResourceEntity resource = resourceRepo.findByPodName(podName);
        if (resource == null) {
            String network = zoneService.fetchNetworkByZoneAndEnv(zone, env);

            List<IpEntity> unoccupiedIpList = ipService.findUnoccupiedIp(network);
            if (unoccupiedIpList.isEmpty()) {
                throw BaseException.newException(MessageType.ERROR, "可用IP不足, network = %s", network);
            }

            for (int i = 0; i < unoccupiedIpList.size(); i++) {
                IpEntity ipEntity = unoccupiedIpList.get(i);
                int n = ipRepo.occupy(ipEntity.getIp());
                if (n > 0) {
                    resourceIp = ipEntity.getIp();
                    ResourceEntity resourceEntity = new ResourceEntity();
                    resourceEntity.setAppId(appId);
                    resourceEntity.setAppName(appName);
                    resourceEntity.setEnv(env);
                    resourceEntity.setZone(zone);
                    resourceEntity.setSpec(spec);
                    resourceEntity.setIp(resourceIp);
                    resourceEntity.setPodName(podName);
                    resourceEntity.setIsStatic(false);
                    resourceRepo.saveAndFlush(resourceEntity);
                    break;
                }
            }
        } else {
            resourceIp = resource.getIp();
        }

        if (resourceIp == null) {
            throw BaseException.newException(MessageType.ERROR, "动态IP分配失败, podName = %s", podName);
        }

        return resourceIp;
    }

    @Transactional(rollbackFor = Exception.class)
    public String setPodNameForStaticResource(String appId, String env, String spec, String ip, String podName) {
        String resourceIp;

        ResourceEntity resource = resourceRepo.findByPodName(podName);
        if (resource == null) {
            ResourceEntity resourceEntity = resourceRepo.findStaticResourceByIp(appId, env, spec, ip);
            if (resourceEntity == null) {
                throw BaseException.newException(MessageType.ERROR, "资源不存在, appId = %s, env = %s, spec = %s, ip = %s", appId, env, spec, ip);
            }
            if (resourceEntity.getPodName() != null) {
                throw BaseException.newException(MessageType.ERROR, "资源[ip=%s]已被实例占用", ip);
            }
            resourceEntity.setPodName(podName);
            resourceRepo.saveAndFlush(resourceEntity);
            resourceIp = resourceEntity.getIp();
        } else {
            resourceIp = resource.getIp();
        }

        if (resourceIp == null) {
            throw BaseException.newException(MessageType.ERROR, "静态IP分配失败, podName = %s", podName);
        }

        return resourceIp;
    }

    @Transactional(rollbackFor = Exception.class)
    public String allocateResourceForPodName(String appId, String appName, String env, String spec, String podName, String zone) {
        String resourceIp = null;

        ResourceEntity resource = resourceRepo.findByPodName(podName);
        if (resource == null) {
            Boolean enableStaticResource = ifAppEnableStaticResource(appId, env);
            if (enableStaticResource == true) {
                List<ResourceEntity> availableStaticResources = resourceRepo.findAvailableStaticResourcesByZone(appId, env, spec, zone);
                if (availableStaticResources.isEmpty()) {
                    throw BaseException.newException(MessageType.ERROR, "可用静态资源不足, appId = %s, env = %s, spec = %s, zone = %s", appId, env, spec, zone);
                }

                for (int i = 0; i < availableStaticResources.size(); i++) {
                    ResourceEntity resourceEntity = availableStaticResources.get(i);
                    int count = resourceRepo.setPodNameForStaticResource(resourceEntity.getId(), podName);
                    if (count > 0) {
                        resourceIp = resourceEntity.getIp();
                        break;
                    }
                }
            } else {
                resourceIp = setPodNameForDynamicResource(appId, appName, env, spec, podName, zone);
            }
        } else {
            resourceIp = resource.getIp();
        }

        if (resourceIp == null) {
            throw BaseException.newException(MessageType.ERROR, "IP分配失败, podName = %s", podName);
        }

        return resourceIp;
    }

    @Transactional(rollbackFor = Exception.class)
    public String setPodNameForResource(String appId, String env, String spec, String ip, String zone, String podName) {
        String resourceIp = null;

        ResourceEntity resource = resourceRepo.findByPodName(podName);
        if (resource == null) {
            // 第一次设置静态资源
            Boolean enableStaticResource = ifAppEnableStaticResource(appId, env);
            if (!enableStaticResource) {
                throw BaseException.newException(MessageType.ERROR, "应用没有设置静态资源, appId = %s", appId);
            }

            ResourceEntity resourceEntity = findByIp(appId, env, spec, ip);
            if (resourceEntity == null) {
                throw BaseException.newException(MessageType.ERROR, "指定IP=%s不在静态资源列表中", ip);
            }

            if (StringUtils.isNotEmpty(resourceEntity.getPodName()) && !resourceEntity.getPodName().equals(podName)) {
                throw BaseException.newException(MessageType.ERROR, "指定IP=%s的静态资源已被其他pod占用, 占用pod = %s",
                        ip, resourceEntity.getPodName());
            }

            if (!zone.equals(resourceEntity.getZone())) {
                throw BaseException.newException(MessageType.ERROR, "指定IP=%s的静态资源不属于实例部署的zone, 静态资源zone = %s, 实例zone = %s ",
                        ip, resourceEntity.getZone(), zone);
            }

            int count = resourceRepo.setPodNameForStaticResource(resourceEntity.getId(), podName);
            if (count == 0) {
                throw BaseException.newException(MessageType.ERROR, "IP分配失败, 数据库更新条数为0, IP = %s, podName = %s", ip, podName);
            }

            resourceIp = resourceEntity.getIp();
        } else {
            // 实例部署失败，setPodNameForResource被重复调用
            resourceIp = resource.getIp();
        }

        if (!ip.equals(resourceIp)) {
            throw BaseException.newException(MessageType.ERROR, "IP分配冲突, 实际IP = %s, 期望IP = %s, podName = %s",
                    resourceIp, ip, podName);
        }

        return resourceIp;
    }

    @Transactional(rollbackFor = Exception.class)
    public void removePodNameFromResource(String podName) {
        ResourceEntity resourceEntity = resourceRepo.findByPodName(podName);
        if (resourceEntity != null) {
            if (resourceEntity.getIsStatic() == true) {
                resourceEntity.setPodName(null);
                resourceRepo.saveAndFlush(resourceEntity);
            } else {
                resourceEntity.setIsActive(false);
                resourceRepo.saveAndFlush(resourceEntity);

                IpEntity ipEntity = ipRepo.findByIp(resourceEntity.getIp());
                if (ipEntity != null) {
                    ipEntity.setOccupied(false);
                    ipRepo.saveAndFlush(ipEntity);
                }
            }
        }
    }

    public Boolean ifAppEnableStaticResource(String appId, String env) {
        List<ResourceEntity> resources = resourceRepo.findStaticResources(appId, env);
        return resources.size() > 0;
    }

    public List<ResourceEntity> findAvailableStaticResources(String appId, String env, String spec) {
        List<ResourceEntity> resourceEntityList = new ArrayList<>();

        List<String> zones = zoneService.fetchZoneNamesByEnv(env);

        for (String zone : zones) {
            List<ResourceEntity> availableStaticResourcesByZone = resourceRepo.findAvailableStaticResourcesByZone(appId, env, spec, zone);
            resourceEntityList.addAll(availableStaticResourcesByZone);
        }

        return resourceEntityList;
    }

    public List<ResourceQuotaStatusVO> fetchResourceQuotaStatus(String appId, String env) {
        List<ResourceQuotaStatusVO> resourceQuotaStatusVOS = new ArrayList<>();
        List<AppQuotaDto> appQuotas = remoteCmdb.fetchAppQuotasByAppAndEnv(appId, env);
        for (AppQuotaDto appQuota : appQuotas) {
            List<ResourceEntity> resources = resourceRepo.findStaticResourcesBySpec(appId, env, appQuota.getSpectypeName());

            ResourceQuotaStatusVO resourceQuotaStatusVO = new ResourceQuotaStatusVO();
            resourceQuotaStatusVO.setSpectypeName(appQuota.getSpectypeName());
            resourceQuotaStatusVO.setTotal(appQuota.getNumber());
            resourceQuotaStatusVO.setUsedCount(Long.valueOf(resources.size()));
            resourceQuotaStatusVO.setFreeCount(appQuota.getNumber() - resources.size());

            resourceQuotaStatusVOS.add(resourceQuotaStatusVO);
        }
        return resourceQuotaStatusVOS;
    }

    @Transactional(rollbackFor = Exception.class)
    public int changeTypeForAllResources(String appId, String env, boolean isStatic) {
        int count;
        if (isStatic == true) {
            count = resourceRepo.changeResourcesType(appId, env, true, false);
        } else {
            count = resourceRepo.changeResourcesType(appId, env, false, true);
        }
        resourceRepo.flush();
        return count;
    }

    public void initResources() {
        List<ResourceEntity> resourceEntityList = new ArrayList<>();
        List<InstanceEntity> instanceEntityList = instanceRepo.findAllInstances();
        for (int i = 0; i < instanceEntityList.size(); i++) {
            InstanceEntity instanceEntity = instanceEntityList.get(i);
            ResourceEntity resource = resourceRepo.findByPodName(instanceEntity.getName());
            if (resource == null) {
                ResourceEntity resourceEntity = new ResourceEntity();
                if (!instanceEntity.getGroupId().equals(0L)) {
                    GroupEntity groupEntity = groupService.getGroupById(instanceEntity.getGroupId());
                    resourceEntity.setAppId(groupEntity.getAppId());
                    resourceEntity.setAppName(groupEntity.getAppName());
                    resourceEntity.setEnv(groupEntity.getEnvironment());
                    resourceEntity.setSpec(groupEntity.getInstanceSpec());
                    resourceEntity.setZone(instanceEntity.getZone());
                } else {
                    resourceEntity.setAppId(instanceEntity.getAppId());
                    resourceEntity.setEnv(instanceEntity.getEnv());
                    resourceEntity.setSpec(instanceEntity.getSpec());
                    resourceEntity.setZone(instanceEntity.getZone());

                    ApplicationEntity applicationEntity = appService.getAppByCmdbId(instanceEntity.getAppId());
                    resourceEntity.setAppName(applicationEntity.getName());
                }

                resourceEntity.setIp(instanceEntity.getSlotIp());
                resourceEntity.setPodName(instanceEntity.getName());
                resourceEntity.setIsStatic(false);
                resourceEntity.setIsActive(true);
                resourceEntityList.add(resourceEntity);
            }
        }
        resourceRepo.save(resourceEntityList);
        resourceRepo.flush();
    }

    public List<ResourceEntity> findResourcesByEnvAndZone(String env, String zone) {
        return resourceRepo.findResourcesByEnvAndZone(env, zone);
    }
}
