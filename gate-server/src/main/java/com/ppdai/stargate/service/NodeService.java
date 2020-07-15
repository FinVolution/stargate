package com.ppdai.stargate.service;

import com.ppdai.stargate.client.K8sClient;
import com.ppdai.stargate.remote.RemoteManager;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class NodeService {

    @Autowired
    private RemoteManager remoteManager;

    public V1Node getNodeByIp(String env, String zone, String hostIp) throws ApiException {
        K8sClient k8sClient = remoteManager.getK8sClientByZone(env, zone);
        return k8sClient.getNodeByIp(hostIp);
    }

    public boolean isNodeDown(V1Node v1Node) {
        Boolean down = false;
        if (v1Node.getStatus().getConditions() != null) {
            Optional<V1NodeCondition> v1NodeCondition = v1Node.getStatus().getConditions()
                    .stream()
                    .filter(x -> x.getType().equals("Ready"))
                    .findFirst();

            if (v1NodeCondition.isPresent()) {
                if (v1NodeCondition.get().getStatus().equals("Unknown")) {
                    down = true;
                }
            }
        }

        return down;
    }
}
