package com.ppdai.stargate.remote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class RemoteRegistryManager {

    @Autowired
    @Qualifier("remoteDefaultConsul")
    private RemoteRegistry remoteDefaultConsul;

    public RemoteRegistry getRemoteRegistryByDomain(String domain) {
        return remoteDefaultConsul;
    }
}
