package com.ppdai.stargate.remote.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ppdai.atlas.client.api.*;
import com.ppdai.atlas.client.model.*;
import com.ppdai.stargate.remote.RemoteCmdb;
import com.ppdai.stargate.utils.ConvertUtil;
import com.ppdai.stargate.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.OffsetDateTime;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RemoteAtlas implements RemoteCmdb {

    @Autowired
    private AppControllerApi atlasAppControllerApi;
    @Autowired
    private OrgControllerApi atlasOrgControllerApi;
    @Autowired
    private SpecTypeControllerApi atlasSpecTypeControllerApi;
    @Autowired
    private UserControllerApi atlasUserControllerApi;
    @Autowired
    private EnvControllerApi atlasEnvControllerApi;
    @Autowired
    private AppQuotaControllerApi atlasAppQuotaControllerApi;
    @Autowired
    private ZoneControllerApi atlasZoneControllerApi;

    Cache<String, List<ZoneDto>> zoneCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();;

    @Override
    public List<InstanceSpecVO> fetchInstanceSpecs() {
        List<SpecTypeDto> specList = null;

        try {
            ResponseListSpecTypeDto allSpecsUsingGET = atlasSpecTypeControllerApi.getAllSpecTypesUsingGET();
            specList = allSpecsUsingGET.getDetail();
        } catch (Exception e) {
            log.error("获取实例规格失败, err=" + e.getMessage(), e);
        } finally {
        }

        List<InstanceSpecVO> specVOs = new ArrayList<>();
        if (specList != null) {
            specVOs = ConvertUtil.convert(specList, InstanceSpecVO.class);
        }

        return specVOs;
    }

    @Override
    public List<AppQuotaDto> fetchAppQuotasByAppAndEnv(String appId, String environment) {
        List<AppQuotaDto> appQuotas = null;

        try {
            ResponseListAppQuotaDto response = atlasAppQuotaControllerApi.findAllAppQuotaByAppAndEnvUsingGET(appId, environment);
            appQuotas = response.getDetail();

        } catch (Exception e) {
            log.error("调用atlas获取应用配额列表失败: appId=" + appId + ", env=" + environment + ", err=" + e.getMessage(), e);
        } finally {
        }

        return appQuotas;
    }

    @Override
    public InstanceSpecVO fetchInstanceSpec(String specName) {
        InstanceSpecVO spec = null;

        List<InstanceSpecVO> specVOList = fetchInstanceSpecs();
        for (InstanceSpecVO instanceSpec : specVOList) {
            if (instanceSpec.getName().equals(specName)) {
                spec = instanceSpec;
                break;
            }
        }

        return spec;
    }

    @Override
    public List<AppVO> fetchAllApps() {
        List<AppDtoPlus> appDtos = null;

        try {
            ResponseListAppDtoPlus allAppPlussUsingGET = atlasAppControllerApi.getAllAppPlussUsingGET(null);
            appDtos = allAppPlussUsingGET.getDetail();
        } catch (Exception e) {
            log.error("获取app列表失败, err=" + e.getMessage(), e);
        } finally {
        }

        List<AppVO> apps = new ArrayList<>();
        if (appDtos != null) {
            apps = ConvertUtil.convert(appDtos, this::applicationMapper);
        }

        return apps;
    }

    @Override
    public List<AppVO> fetchAppsByUsername(String username) {
        List<AppDtoPlus> appDtos = null;

        try {
            ResponseListAppDtoPlus appsByUserNameUsingGET = atlasAppControllerApi.findAppsByUserNameUsingGET(username);
            appDtos = appsByUserNameUsingGET.getDetail();
        } catch (Exception e) {
            log.error("根据负责人获取app列表失败, username=" + username + ", err=" + e.getMessage(), e);
        } finally {
        }

        List<AppVO> apps = new ArrayList<>();
        if (appDtos != null) {
            apps = ConvertUtil.convert(appDtos, this::applicationMapper);
        }

        return apps;
    }

    @Override
    public AppVO fetchAppByAppId(String appId) {
        AppDtoPlus appDto = null;

        try {
            ResponsePageDtoAppDtoPlus response = atlasAppControllerApi.getAppPlusByConditionsUsingGET(0, 1, appId, null, null, null);
            if (response.getDetail().getContent().size() > 0) {
                appDto = response.getDetail().getContent().get(0);
            }

        } catch (Exception e) {
            log.error("根据appId获取app列表失败, " + appId +", err=" + e.getMessage(), e);
        } finally {
        }

        AppVO appVO = new AppVO();
        if (appDto != null) {
            appVO = applicationMapper(appDto);
        }

        return appVO;
    }

    private AppVO applicationMapper(AppDto appDto) {
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(appDto, appVO);
        appVO.setCmdbAppId(appDto.getAppId());
        appVO.setServiceType(appDto.getServiceType());
        appVO.setAppType(appDto.getAppType());
        appVO.setEnableHa(appDto.isEnableHa());

        OrgDto orgDto = appDto.getOrgDto();
        if (orgDto != null) {
            appVO.setDepartment(orgDto.getName());
            appVO.setDepartmentCode(orgDto.getOrgCode());
        }

        UserDto userDto = appDto.getUserDto();
        if (userDto != null) {
            appVO.setOwner(userDto.getUserName());
        }

        DateTime dateTime = appDto.getInsertTime();
        if (dateTime != null) {
            appVO.setInsertTime(dateTime.toDate());
        }

        List<EnvUrl> envUrls = appDto.getEnvUrls();
        Map<String, String> envMap = new HashMap<>();
        if (envUrls != null) {
            envUrls.forEach(envUrl -> {
                envMap.put(envUrl.getEnvName(), envUrl.getUrl());
            });
        }
        appVO.setEnvUrlMap(envMap);

        return appVO;
    }

    private AppVO applicationMapper(AppDtoPlus appDto) {
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(appDto, appVO);
        appVO.setCmdbAppId(appDto.getAppId());
        appVO.setServiceType(appDto.getServiceType());
        appVO.setAppType(appDto.getAppType());
        appVO.setEnableHa(appDto.isEnableHa());

        OrgDto orgDto = appDto.getOrgDto();
        if (orgDto != null) {
            appVO.setDepartment(orgDto.getName());
            appVO.setDepartmentCode(orgDto.getOrgCode());
        }

        List<String> ownerList = new ArrayList<>();
        List<String> developerList = new ArrayList<>();
        List<String> developerNameList = new ArrayList<>();
        List<String> testerList = new ArrayList<>();
        List<String> testerNameList = new ArrayList<>();

        List<UserDto> userDtos = appDto.getUserDtos();
        if (userDtos != null) {
            userDtos.forEach(userDto -> {
                if (!ownerList.contains(userDto.getUserName())) {
                    ownerList.add(userDto.getUserName());
                }
                developerList.add(userDto.getUserName());
                developerNameList.add(userDto.getRealName());
            });
        }

        List<UserDto> testUserDtos = appDto.getTestUserDtos();
        if (testUserDtos != null) {
            testUserDtos.forEach(testUserDto -> {
                if (!ownerList.contains(testUserDto.getUserName())) {
                    ownerList.add(testUserDto.getUserName());
                }
                testerList.add(testUserDto.getUserName());
                testerNameList.add(testUserDto.getRealName());
            });
        }

        String owners = StringUtils.join(ownerList.toArray(), ",");
        String developers = StringUtils.join(developerList.toArray(), ",");
        String developerNames = StringUtils.join(developerNameList.toArray(), " ");
        String testers = StringUtils.join(testerList.toArray(), ",");
        String testerNames = StringUtils.join(testerNameList.toArray(), " ");
        appVO.setOwner(owners);
        appVO.setDeveloper(developers);
        appVO.setDeveloperNames(developerNames);
        appVO.setTester(testers);
        appVO.setTesterNames(testerNames);

        DateTime dateTime = appDto.getInsertTime();
        if (dateTime != null) {
            appVO.setInsertTime(dateTime.toDate());
        }

        List<EnvUrl> envUrls = appDto.getEnvUrls();
        Map<String, String> envMap = new HashMap<>();
        if (envUrls != null) {
            envUrls.forEach(envUrl -> {
                envMap.put(envUrl.getEnvName(), envUrl.getUrl());
            });
        }
        appVO.setEnvUrlMap(envMap);

        return appVO;
    }

    @Override
    public List<OrgVO> fetchOrganizations() {
        List<OrgDto> orgDtos = null;

        try {
            ResponseListOrgDto responseListOrgDto = atlasOrgControllerApi.getAllOrgsUsingGET();
            orgDtos = responseListOrgDto.getDetail();

        } catch (Exception e) {
            log.error("获取组织列表失败, err=" + e.getMessage(), e);
        } finally {
        }

        List<OrgVO> orgVOs = new ArrayList<>();
        if (orgDtos != null) {
            orgVOs = ConvertUtil.convert(orgDtos, OrgVO.class);
        }
        return orgVOs;
    }

    @Override
    public List<UserDto> searchUsersByUserName(String userName) {
        List<UserDto> userDtos = null;

        try {
            ResponseListUserDto response = atlasUserControllerApi.fuzzySearchUsersByUserNameUsingGET(userName);
            userDtos = response.getDetail();

        } catch (Exception e) {
            log.error("调用atlas查询用户列表失败: userName=" + userName + ", err=" + e.getMessage(), e);
        } finally {
        }

        return userDtos;
    }

    @Override
    public Boolean updateAppMember(String appId, String developers, String testers) {
        Boolean isSuccess = null;

        try {
            AppManagerQuery appManagerQuery = new AppManagerQuery();
            appManagerQuery.setAppId(appId);
            appManagerQuery.setDevelopers(developers);
            appManagerQuery.setTesters(testers);
            Responseboolean response = atlasAppControllerApi.updateAppManagerUsingPUT(appManagerQuery);
            isSuccess = response.isDetail();
        } catch (Exception e) {
            log.error("调用atlas更新应用成员失败: appId=" + appId + ", developers=" + developers + ", testers=" + testers + ", err=" + e.getMessage(), e);
        } finally {
        }

        return isSuccess;
    }

    @Override
    public List<ZoneDto> fetchZonesByEnv(String env) {
        List<ZoneDto> zoneDtos = zoneCache.getIfPresent(env);
        if (zoneDtos != null)
            return zoneDtos;

        try {
            ResponseListZoneDto responseListZoneDto = atlasZoneControllerApi.getByEnvUsingGET(env);
            zoneDtos = responseListZoneDto.getDetail();

        } catch (Exception e) {
            log.error("根据环境获取zone列表失败, env=" + env + ", err=" + e.getMessage(), e);
        } finally {
        }

        return zoneDtos;
    }

    @Override
    public List<ZoneDto> fetchAllZones() {
        List<ZoneDto> zoneDtos = null;

        try {
            ResponseListZoneDto responseListZoneDto = atlasZoneControllerApi.getAllZonesUsingGET();
            zoneDtos = responseListZoneDto.getDetail();

        } catch (Exception e) {
            log.error("获取zone列表失败, err=" + e.getMessage(), e);
        } finally {
        }

        return zoneDtos;
    }

    @Override
    public List<EnvVO> fetchEnvironments() {
        List<EnvDto> remoteEnvList = null;

        try {
            ResponseListEnvDto remoteResponse = atlasEnvControllerApi.getAllEnvsUsingGET();
            remoteEnvList = remoteResponse.getDetail();

        } catch (Exception e) {
            log.error("获取环境列表失败, err=" + e.getMessage(), e);
        } finally {
        }

        List<EnvVO> envVOs = new ArrayList<>();
        if (remoteEnvList != null) {
            envVOs = ConvertUtil.convert(remoteEnvList, this::environmentMapper);
        }

        return envVOs;
    }

    private EnvVO environmentMapper(EnvDto envDto) {
        EnvVO envVO = new EnvVO();
        BeanUtils.copyProperties(envDto, envVO);

        DateTime dateTime = envDto.getInsertTime();
        if (dateTime != null) {
            envVO.setInsertTime(dateTime.toDate());
        }

        return envVO;
    }

}
