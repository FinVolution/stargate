package com.ppdai.stargate.remote;

import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.vo.InstanceV2VO;
import com.ppdai.stargate.vo.InstanceVO;

import java.io.IOException;
import java.util.List;

public interface RemoteRegistry {

    void register(String env, String appId, String appName, String domain, InstanceEntity instanceEntity);

    void deregister(String env, String appId, String appName, String domain, InstanceEntity instanceEntity);


    /**
     * 拉入实例（接流量）
     * @param domain
     * @param instance
     */
    void pullIn(String domain, InstanceEntity instance);

    /**
     * 拉出实例（摘流量）
     * @param domain
     * @param instance
     * @throws IOException
     */
    void pullOut(String domain, InstanceEntity instance);

    /**
     * 向Remote Registry中查看实例运行状态
     *
     * @return 返回服务器实例列表
     */
    List<InstanceVO> getInstanceStatus(String environment, String domain, List<InstanceEntity> instances);

    InstanceV2VO getInstanceStatus(String domain, String environment, String appId, String appName, InstanceEntity instanceEntity);

}
