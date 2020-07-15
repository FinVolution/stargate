package com.ppdai.stargate.remote.impl;

import com.ppdai.stargate.client.ConsulClient;
import com.ppdai.stargate.remote.RemoteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RemoteDefaultConsul extends RemoteConsul {

    @Autowired
    private RemoteManager remoteManager;

    @Override
    protected String getUpstream(String domain) {
        return domain.replace(".", "_")
                .replace("dev-", "")
                .replace("fat-", "")
                .replace("uat-", "")
                .replace("lpt-", "")
                .replace("pre-", "") + "_server";
    }

    @Override
    protected ConsulClient getConsulClient(String env, String appId, String appName) {
        return remoteManager.getConsulClient(env);
    }
}
