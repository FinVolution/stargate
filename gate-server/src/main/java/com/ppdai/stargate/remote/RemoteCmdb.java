package com.ppdai.stargate.remote;

import com.ppdai.atlas.client.model.AppQuotaDto;
import com.ppdai.atlas.client.model.UserDto;
import com.ppdai.atlas.client.model.ZoneDto;
import com.ppdai.stargate.vo.*;

import java.util.List;

public interface RemoteCmdb {

    /**
     * 获取应用服务列表
     *
     * @return 返回应用列表
     */
    public List<AppVO> fetchAllApps();

    /**
     * 获取指定用户负责的应用列表
     *
     * @return 返回应用列表
     */
    public List<AppVO> fetchAppsByUsername(String username);

    /**
     * 获取支持的实例规格列表，包括CPU/Disk/Memory的配置
     *
     * @return 返回实例规格列表
     */
    public List<InstanceSpecVO> fetchInstanceSpecs();

    /**
     * 根据应用Id和环境名获取配额列表
     * @param appId 应用Id
     * @param environment 环境名
     * @return 返回配额列表
     */
    public List<AppQuotaDto> fetchAppQuotasByAppAndEnv(String appId, String environment);

    /**
     * 根据配置规格名，获取CPU/Disk/Memory的配置
     *
     * @param specName 规格名
     * @return 返回实例规格，若没有找到，返回null
     */
    public InstanceSpecVO fetchInstanceSpec(String specName);

    /**
     * 获取环境列表
     *
     * @return 返回环境列表
     */
    public List<EnvVO> fetchEnvironments();

    /**
     * 获取组织列表
     *
     * @return 返回组织列表
     */
    public List<OrgVO> fetchOrganizations();

    /**
     * 获取指定appId的应用
     *
     * @param appId
     * @return
     */
    public AppVO fetchAppByAppId(String appId);

    /**
     * 根据用户名模糊查询用户列表
     * @param userName 用户名
     * @return
     */
    public List<UserDto> searchUsersByUserName(String userName);

    /**
     * 更新应用的成员
     * @param appId 应用ID
     * @param developers 开发人员域账号
     * @param testers 测试人员域账号
     */
    public Boolean updateAppMember(String appId, String developers, String testers);

    /**
     * 获取指定环境的zone列表
     * @param env 环境名
     * @return
     */
    public List<ZoneDto> fetchZonesByEnv(String env);

    /**
     * 获取所有的zone列表
     * @return
     */
    public List<ZoneDto> fetchAllZones();
}
