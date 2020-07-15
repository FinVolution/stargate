package com.ppdai.stargate.service;

import com.ppdai.atlas.client.model.AppQuotaDto;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.dao.*;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.po.ResourceEntity;
import com.ppdai.stargate.po.converter.EnvUrl;
import com.ppdai.stargate.remote.RemoteCmdb;
import com.ppdai.stargate.utils.ConvertUtil;
import com.ppdai.stargate.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.*;

@Service
@Slf4j
public class AppService {

    @Autowired
    private ApplicationRepository appRepo;
    @Autowired
    private InstanceRepository instanceRepo;
    @Autowired
    private ResourceRepository resourceRepo;
    @Autowired
    private RemoteCmdb remoteCmdb;
    @Autowired
    private EnvService envService;
    @Autowired
    private ZoneService zoneService;
    @Autowired
    private ResourceService resourceService;

    /**
     * 获取应用服务列表
     *
     * @return 返回应用服务实体列表
     */
    public List<AppVO> fetchAllApps() {
        return remoteCmdb.fetchAllApps();
    }

    public AppVO fetchAppByAppId(String appId) {
        return remoteCmdb.fetchAppByAppId(appId);
    }

    public List<AppVO> fetchAppsByUsername(String username) {
        List<AppVO> appVOS = remoteCmdb.fetchAppsByUsername(username);
        appVOS.sort(Comparator.comparing(AppVO::getName));
        return appVOS;
    }

    public List<AppVO> fetchAllAppsFromLocalDB() {
        List<ApplicationEntity> apps = appRepo.findAllApps();
        List<AppVO> appVOS = ConvertUtil.convert(apps, AppVO.class);
        appVOS.sort(Comparator.comparing(AppVO::getName));
        return appVOS;
    }

    public List<AppVO> fetchAppsByDepartmentFromLocalDB(String department) {
        List<ApplicationEntity> apps = appRepo.findByDepartment(department);
        List<AppVO> appVOS = ConvertUtil.convert(apps, AppVO.class);
        appVOS.sort(Comparator.comparing(AppVO::getName));
        return appVOS;
    }

    /**
     * 获取应用服务分页列表
     *
     * @param page    页码
     * @param size    每页数量
     * @param appId   应用Id
     * @return 返回应用服务实体分页列表
     */
    public PageVO<ApplicationEntity> fetchAppsByPage(Integer page, Integer size, String appId) {
        PageVO<ApplicationEntity> appPageVO = new PageVO<>();

        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(page - 1, size, sort);

        Page<ApplicationEntity> appPage = appRepo.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();

            if (StringUtils.isNotEmpty(appId)) {
                list.add(criteriaBuilder.equal(root.get("cmdbAppId").as(String.class), appId));
            }

            list.add(criteriaBuilder.equal(root.get("isActive").as(Boolean.class), Boolean.TRUE));

            Predicate[] p = new Predicate[list.size()];
            return criteriaBuilder.and(list.toArray(p));
        }, pageable);

        appPageVO.setContent(appPage.getContent());
        appPageVO.setTotalElements(appPage.getTotalElements());

        return appPageVO;
    }

    public void removeAppById(Long id) {
        ApplicationEntity applicationEntity = appRepo.findOne(id);
        if (applicationEntity != null) {
            List<InstanceEntity> instances = instanceRepo.findInstancesByAppId(applicationEntity.getCmdbAppId());
            if (!instances.isEmpty()) {
                throw BaseException.newException(MessageType.ERROR, "应用还有实例未销毁，数量=" + instances.size());
            }

            List<ResourceEntity> resources = resourceRepo.findByAppId(applicationEntity.getCmdbAppId());
            if (!resources.isEmpty()) {
                throw BaseException.newException(MessageType.ERROR, "应用还有资源未释放，数量=" + resources.size());
            }

            // 删除app
            applicationEntity.setIsActive(false);
            appRepo.save(applicationEntity);
        }
    }

    public void updateAllApps(List<AppVO> appVOs) {
        final int[] syncAppCnt = {0};

        appVOs.forEach(appVO -> {
            ApplicationEntity appInDB = appRepo.findByAppIdEx(appVO.getCmdbAppId());
            if (appInDB == null) {
                // 若数据库没有该条应用，则创建
                appInDB = new ApplicationEntity();
            }

            String oldAppName = appInDB.getName();

            // 保留数据库中的id/insertTime/updateTime，其它以app信息为准，然后保存
            BeanUtils.copyProperties(appVO, appInDB, "id", "insertTime", "updateTime", "envUrls");

            List<EnvUrl> envUrls = new ArrayList<>();
            for (Map.Entry<String, String> entry : appVO.getEnvUrlMap().entrySet()) {
                EnvUrl envUrl = new EnvUrl();
                envUrl.setEnvName(entry.getKey());
                envUrl.setUrl(entry.getValue());
                envUrls.add(envUrl);
            }

            appInDB.setEnvUrls(envUrls);
            appInDB.setIsActive(true);
            appRepo.save(appInDB);
            syncAppCnt[0]++;

            //todo 更新其他表的应用名
            if (oldAppName != null && !appVO.getName().equals(oldAppName)) {

            }
        });

        log.info("同步应用数={}", syncAppCnt[0]);
    }

    /**
     * 同步应用列表到本地数据库
     *
     * @return 若同步成功，则返回True
     */
    public Boolean syncAllApps() {

        Boolean bSuccess = false;

        try {
            // 同步应用列表
            List<AppVO> appVOs = fetchAllApps();
            updateAllApps(appVOs);

            bSuccess = Boolean.TRUE;

        } catch (Exception e) {
            log.error("同步应用列表时发生错误, err=" + e.getMessage(), e);
        }

        return bSuccess;
    }

    public ApplicationEntity getAppByCmdbId(String appCmdbId) {
        return appRepo.findByAppId(appCmdbId);
    }

    public String getAppDomainByEnv(String appCmdbId, String env) {
        ApplicationEntity applicationEntity = appRepo.findByAppId(appCmdbId);
        for (EnvUrl envUrl : applicationEntity.getEnvUrls()) {
            if (envUrl.getEnvName().equals(env)) {
                return envUrl.getUrl();
            }
        }

        return null;
    }

    public String getAppDomainByEnv(ApplicationEntity applicationEntity, String env) {
        for (EnvUrl envUrl : applicationEntity.getEnvUrls()) {
            if (envUrl.getEnvName().equals(env)) {
                return envUrl.getUrl();
            }
        }

        return null;
    }

    public ApplicationEntity getAppByName(String appName) {
        return appRepo.findByName(appName);
    }

    public List<AppQuotaDto> fetchAppQuotasByAppAndEnv(String appId, String environment) {
        List<AppQuotaDto> appQuotas = remoteCmdb.fetchAppQuotasByAppAndEnv(appId, environment);
        return appQuotas;
    }

    public List<AppQuotaStatusVO> fetchAppQuotaStatusByAppAndEnv(String appId, String environment) {
        List<AppQuotaStatusVO> appQuotaStatusVOS = new ArrayList<>();
        List<AppQuotaDto> appQuotas = remoteCmdb.fetchAppQuotasByAppAndEnv(appId, environment);

        for (AppQuotaDto appQuota : appQuotas) {

            long usedCount = 0;

            // 添加私有云发布的实例
            List<InstanceEntity> instancesByEnvAndAppIdAndSpec = instanceRepo.findInstancesByEnvAndAppIdAndSpec(environment, appId, appQuota.getSpectypeName());
            usedCount += instancesByEnvAndAppIdAndSpec.size();

            AppQuotaStatusVO appQuotaStatusVO = new AppQuotaStatusVO();
            appQuotaStatusVO.setSpectypeName(appQuota.getSpectypeName());
            appQuotaStatusVO.setTotal(appQuota.getNumber());
            appQuotaStatusVO.setUsedCount(usedCount);
            appQuotaStatusVO.setFreeCount(appQuota.getNumber() - usedCount);

            appQuotaStatusVOS.add(appQuotaStatusVO);
        }

        return appQuotaStatusVOS;
    }

    public Boolean updateAppMember(String appId, String developers, String testers) {
        String[] developerArr = developers.split(",");
        String[] testerArr = testers.split(",");
        List<String> developerList = new ArrayList<>();
        List<String> testerList = new ArrayList<>();
        for (int i = 0; i < developerArr.length; i++) {
            if (!developerList.contains(developerArr[i])) {
                developerList.add(developerArr[i]);
            }
        }
        for (int i = 0; i < testerArr.length; i++) {
            if (!testerList.contains(testerArr[i])) {
                testerList.add(testerArr[i]);
            }
        }
        return remoteCmdb.updateAppMember(appId, StringUtils.join(developerList, ","), StringUtils.join(testerList, ","));
    }

    public SiteStatusVO fetchSiteStatus(String env, String appId) {
        SiteStatusVO siteStatusVO = new SiteStatusVO();

        ApplicationEntity applicationEntity = getAppByCmdbId(appId);
        EnvVO envVO = envService.queryInUseEnvironment(env);

        if (applicationEntity != null && envVO != null) {
            siteStatusVO.setEnvironment(env);
            siteStatusVO.setAppId(appId);
            siteStatusVO.setAppName(applicationEntity.getName());
            siteStatusVO.setOwner(applicationEntity.getOwner());

            String domain = getAppDomainByEnv(applicationEntity, env);
            siteStatusVO.setDomain(domain);

            Boolean bool = resourceService.ifAppEnableStaticResource(appId, env);
            siteStatusVO.setEnableStaticResource(bool);

            List<String> zones = zoneService.fetchZoneNamesByEnv(env);
            siteStatusVO.setZones(zones);
        }

        return siteStatusVO;
    }
}
