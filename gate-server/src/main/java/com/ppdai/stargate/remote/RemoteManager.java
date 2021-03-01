package com.ppdai.stargate.remote;

import com.ppdai.atlas.client.model.ZoneDto;
import com.ppdai.stargate.client.ConsulClient;
import com.ppdai.stargate.client.JsonHttpClient;
import com.ppdai.stargate.client.K8sClient;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.service.EnvService;
import com.ppdai.stargate.vo.EnvVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RemoteManager {

    @Autowired
    private RemoteCmdb remoteCmdb;

    @Autowired
    private JsonHttpClient consulHttpClient;

    @Autowired
    private Environment environment;

    @Autowired
    private EnvService envService;

    public List<K8sClient> getK8sClientsByEnv(String env) {
        List<K8sClient> k8sClients = new ArrayList<>();

        List<ZoneDto> zoneDtos = remoteCmdb.fetchZonesByEnv(env);
        for (int i = 0; i < zoneDtos.size(); i++) {
            ZoneDto zoneDto = zoneDtos.get(i);

            log.info("<<getK8sClientsByEnv>> 按环境和部署区域获取K8s信息: env={}, zone={}, k8s={}", env, zoneDto.getName(), zoneDto.getK8s());
            K8sClient k8sClient = new K8sClient(env, zoneDto.getK8s(), "");
            k8sClients.add(k8sClient);
        }

        return k8sClients;
    }

    public K8sClient getK8sClientByZone(String env, String zone) {
        K8sClient k8sClient = null;

        List<ZoneDto> zoneDtos = remoteCmdb.fetchZonesByEnv(env);
        for (int i = 0; i < zoneDtos.size(); i++) {
            ZoneDto zoneDto = zoneDtos.get(i);
            if (zoneDto.getName().equals(zone)) {
                String k8sUrl = zoneDto.getK8s();
                log.info("<<getK8sClientByZone>> 按环境和部署区域获取K8s信息: env={}, zone={}, k8s={}", env, zone, k8sUrl);
                k8sClient = new K8sClient(env, k8sUrl, "");
                break;
            }
        }

        if (k8sClient == null) {
            throw new RuntimeException("没有找到K8s信息, env=" + env + ", zone=" + zone);
        }

        return k8sClient;
    }

    public K8sClient getK8sClientRpcByZone(String env, String zone) {
        K8sClient k8sClient = null;

        List<ZoneDto> zoneDtos = remoteCmdb.fetchZonesByEnv(env);
        for (int i = 0; i < zoneDtos.size(); i++) {
            ZoneDto zoneDto = zoneDtos.get(i);
            if (zoneDto.getName().equals(zone)) {
                Object k8sUrl = ((Map)zoneDto.getExtensions()).get("rpc");
                if (k8sUrl == null) {
                    throw new RuntimeException("没有找到K8srpc信息, env=" + env + ", zone=" + zone);
                }

                log.info("<<getK8sClientByZone>> 按环境和部署区域获取K8s信息: env={}, zone={}, k8srpc={}", env, zone, k8sUrl.toString());
                k8sClient = new K8sClient(env, k8sUrl.toString(), "");
                break;
            }
        }

        if (k8sClient == null) {
            throw new RuntimeException("没有找到K8s信息, env=" + env + ", zone=" + zone);
        }

        return k8sClient;
    }

    /**
     *
     * @param env
     * @return
     */
    public ConsulClient getConsulClient(String env) {

        EnvVO envVO = envService.queryInUseEnvironment(env);
        if (envVO == null) {
            throw BaseException.newException(MessageType.ERROR, "环境不可用, env=%s", env);
        }

        log.info("<<getConsulClient>> 按环境获取consul信息: env={}, consul={}", env, envVO.getConsul());
        return new ConsulClient(envVO.getConsul(), consulHttpClient);
    }

    public String getK8sMasterRpcUrl(String env, String zone) {
        String rpc = "";
        List<ZoneDto> zoneDtos = remoteCmdb.fetchZonesByEnv(env);
        for (int i = 0; i < zoneDtos.size(); i++) {
            ZoneDto zoneDto = zoneDtos.get(i);
            if (zoneDto.getName().equals(zone)) {
                String k8sUrl = zoneDto.getK8s();
                log.info("<<getK8sClientByZone>> 按环境和部署区域获取K8s信息: env={}, zone={}, k8s={}", env, zone, k8sUrl);
                Object extensions = zoneDto.getExtensions();
                if (extensions instanceof Map) {
                    Map<String, Object> k8sInfoMap = (Map) extensions;
                    Object k8sRpcObj = k8sInfoMap.get("rpc");
                    rpc = k8sRpcObj.toString();
                }
                break;
            }
        }
        return rpc;
    }
}
