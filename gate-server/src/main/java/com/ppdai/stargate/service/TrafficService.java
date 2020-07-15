package com.ppdai.stargate.service;

import com.ppdai.stargate.dao.InstanceRepository;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.remote.RemoteRegistry;
import com.ppdai.stargate.remote.RemoteRegistryManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrafficService {

    @Autowired
    private RemoteRegistryManager remoteRegistryManager;

    @Autowired
    private InstanceRepository instanceRepo;

    private RemoteRegistry getRemoteRegistry(String domain) {
        RemoteRegistry remoteRegistry = remoteRegistryManager.getRemoteRegistryByDomain(domain);
        if (remoteRegistry == null) {
            throw new IllegalArgumentException("无法找到remoteRegistry, domain=" + domain);
        }

        return remoteRegistry;
    }

    public void registerOne(String env, String appId, String appName, String domain, InstanceEntity instanceEntity) {
        RemoteRegistry remoteRegistry = getRemoteRegistry(domain);

        remoteRegistry.register(env, appId, appName, domain, instanceEntity);
    }

    public void deregisterOne(String env, String appId, String appName, String domain, InstanceEntity instanceEntity) {
        RemoteRegistry remoteRegistry = getRemoteRegistry(domain);

        remoteRegistry.deregister(env, appId, appName, domain, instanceEntity);
    }

    public void pullInOne(String domain, InstanceEntity instanceEntity) {
        RemoteRegistry remoteRegistry = getRemoteRegistry(domain);

        remoteRegistry.pullIn(domain, instanceEntity);

        instanceEntity.setHasPulledIn(Boolean.TRUE);
        instanceRepo.save(instanceEntity);
    }

    public void pullOutOne(String domain, InstanceEntity instanceEntity) {
        RemoteRegistry remoteRegistry = getRemoteRegistry(domain);

        remoteRegistry.pullOut(domain, instanceEntity);

        instanceEntity.setHasPulledIn(Boolean.FALSE);
        instanceRepo.save(instanceEntity);
    }
}
