package com.ppdai.stargate.service.flink;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.ContainerService;
import com.ppdai.stargate.service.InstanceService;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodCondition;
import io.kubernetes.client.models.V1PodList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by chenlang on 2020/9/2
 **/
@Slf4j
@Service
public class PhoenixClientService {

    @Autowired
    private InstanceService instanceService;
    @Autowired
    private ContainerService containerService;

    public String phoenixAddress(String env, String zone) {
        return "http://" + phoenixIp(env, zone) + ":" + phoenixPort();
    }

    public String phoenixIp(String env, String zone) {
        List<String> ips = phoenixIps(env, zone);
        return ips.stream().findAny().orElse(null);
    }

    public List<String> phoenixIps(String env, String zone) {
        List<InstanceEntity> instances = instanceService.findByEnvAndAppId(env, "9999999001");
        return instances.stream().filter(instanceEntity -> podIsReady(instanceEntity) && instanceEntity.getZone().equalsIgnoreCase(zone))
                .map(InstanceEntity::getSlotIp).collect(Collectors.toList());
    }

    public Integer phoenixPort() {
        return 8009;
    }


    public boolean podIsReady(InstanceEntity instanceEntity) {
        V1PodList v1PodList;
        try {
            v1PodList = containerService.getContainersByInstanceName(instanceEntity.getEnv(), instanceEntity.getNamespace(),
                    instanceEntity.getName(), instanceEntity.getZone());
        } catch (ApiException e) {
            log.error("查询 phoenix Pod失败, err=" + e.getMessage(), e);
            throw BaseException.newException(MessageType.ERROR, "查询Pod失败, err=" + e.getMessage());
        }
        Optional<V1Pod> podOpt = v1PodList.getItems()
                .stream()
                .filter(x -> x.getMetadata().getName().equals(instanceEntity.getName()))
                .findFirst();
        V1Pod v1Pod = null;
        if (podOpt.isPresent()) {
            v1Pod = podOpt.get();
        }

        if (v1Pod != null && v1Pod.getStatus().getConditions() != null) {
            Optional<V1PodCondition> v1PodCondition = v1Pod.getStatus().getConditions()
                    .stream()
                    .filter(x -> x.getType().equals("Ready"))
                    .findFirst();

            if (v1PodCondition.isPresent() && v1PodCondition.get().getStatus().equals("True")) {
                return true;
            }
        }
        return false;
    }

}
