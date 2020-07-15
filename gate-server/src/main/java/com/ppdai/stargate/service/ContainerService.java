package com.ppdai.stargate.service;

import com.ppdai.stargate.client.K8sClient;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.remote.RemoteManager;
import com.ppdai.stargate.vi.AppPodTemplateVI;
import com.ppdai.stargate.vo.*;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class ContainerService {

    @Autowired
    private RemoteManager remoteManager;
    @Autowired
    private Environment environment;

    private K8sQuotaVO getResource(String env, String spec) {

        K8sQuotaVO k8sQuotaVO = new K8sQuota2C4GVO();
        if (spec.toUpperCase().equals("D-4C8G")) {
            k8sQuotaVO = new K8sQuota4C8GVO();
        } else if (spec.toUpperCase().equals("B-2C2G")) {
            k8sQuotaVO = new K8sQuota2C2GVO();
        }

        Boolean cpuOversubscribed = Boolean.parseBoolean(environment.getProperty("k8s.cpu.oversubscribe", "true"));
        String cpuOversubscribedValue = environment.getProperty("k8s.cpu.oversubscribe.value", "0.5");

        String memoryOversubscribedEnvs = environment.getProperty("k8s.memory.oversubscribe.envs", "fat");
        String memoryOversubscribedPercent = environment.getProperty("k8s.memory.oversubscribe.percent", "0.5");
        Set<String> memoryOversubscribedSet = new HashSet<>();

        for (String envStr : memoryOversubscribedEnvs.split(",")) {
            memoryOversubscribedSet.add(envStr.trim().toLowerCase());
        }

        K8sQuotaVO finalK8sQuotaVO = k8sQuotaVO;
        return new K8sQuotaVO() {
            @Override
            public Quantity getRequestCpu() {
                if (cpuOversubscribed) {
                    return new Quantity(cpuOversubscribedValue);
                } else {
                    return finalK8sQuotaVO.getRequestCpu();
                }
            }

            @Override
            public Quantity getRequestMemory() {
                if (memoryOversubscribedSet.contains(env.toLowerCase())) {
                    return new Quantity(finalK8sQuotaVO.getLimitMemory().getNumber().multiply(new BigDecimal(memoryOversubscribedPercent)),
                            finalK8sQuotaVO.getLimitMemory().getFormat());
                } else {
                    return finalK8sQuotaVO.getRequestMemory();
                }
            }

            @Override
            public Quantity getLimitCpu() {
                return finalK8sQuotaVO.getLimitCpu();
            }

            @Override
            public Quantity getLimitMemory() {
                return finalK8sQuotaVO.getLimitMemory();
            }

            @Override
            public String getScope() {
                return finalK8sQuotaVO.getScope();
            }

            @Override
            public String getJavaOpts() {
                return finalK8sQuotaVO.getJavaOpts();
            }
        };
    }

    public String execCommand(String[] commands, String env, String zone, String namespace, String instance) throws ApiException, IOException, InterruptedException {
        K8sClient k8sClient = remoteManager.getK8sClientRpcByZone(env, zone);

        return k8sClient.execCommand(instance, namespace, commands);
    }

    public void createNamespace(String env, String zone, String namespace) throws ApiException {
        K8sClient k8sClient = remoteManager.getK8sClientRpcByZone(env, zone);

        k8sClient.createNamespace(namespace);
    }

    public void createService(String env, String zone, String appName, String namespace) throws ApiException {
        K8sClient k8sClient = remoteManager.getK8sClientByZone(env, zone);

        k8sClient.createService(appName, namespace);
    }

    /**
     * 创建ConfigMap
     * @param env
     * @param zone
     * @param namespace
     * @param configName
     * @param dataMap
     * @throws ApiException
     */
    public void createConfigMap(String env,  String zone, String namespace, String configName, Map<String, String> dataMap) throws ApiException {
        K8sClient k8sClient = remoteManager.getK8sClientByZone(env, zone);

        k8sClient.createConfigMap(namespace, configName, dataMap);
    }

    /**
     * 获取ConfigMap
     * @param env
     * @param zone
     * @param namespace
     * @param configName
     * @return
     * @throws ApiException
     */
    public V1ConfigMap getConfigMap(String env, String zone, String namespace, String configName) throws ApiException {
        K8sClient k8sClient = remoteManager.getK8sClientByZone(env, zone);

        return k8sClient.getConfigMap(namespace, configName);
    }

    /**
     * 删除ConfigMap
     * @param env
     * @param zone
     * @param namespace
     * @param configName
     * @throws ApiException
     */
    public void deleteConfigMap(String env, String zone, String namespace, String configName) throws ApiException {
        K8sClient k8sClient = remoteManager.getK8sClientByZone(env, zone);

        k8sClient.deleteConfigMap(namespace, configName);
    }

    public void startContainer(String dockerUrl, String dns, InstanceEntity instanceEntity) throws ApiException {
        // 检查ip是否被其他容器占用
        V1PodList v1PodList = getContainersByPodIp(instanceEntity.getEnv(), instanceEntity.getSlotIp());
        if (!v1PodList.getItems().isEmpty()) {
            String podName = v1PodList.getItems().get(0).getMetadata().getName();
            if (!podName.equals(instanceEntity.getName())) {
                throw BaseException.newException(MessageType.ERROR, "实例IP已被其他容器使用, IP=%s, podName=%s", instanceEntity.getSlotIp(), podName);
            }
        }

        K8sClient k8sClient = remoteManager.getK8sClientByZone(instanceEntity.getEnv(), instanceEntity.getZone());


        AppPodTemplateVI appPodTemplateVI = new AppPodTemplateVI();
        appPodTemplateVI.setAppId(instanceEntity.getAppId());
        appPodTemplateVI.setAppName(instanceEntity.getAppName());
        appPodTemplateVI.setDns(dns);
        appPodTemplateVI.setImage(dockerUrl + "/" + instanceEntity.getImage());
        appPodTemplateVI.setK8sQuotaVO(getResource(instanceEntity.getEnv(), instanceEntity.getSpec()));
        appPodTemplateVI.setFlags(instanceEntity.getFlags());
        appPodTemplateVI.setEnvJson(instanceEntity.getEnvVars());
        appPodTemplateVI.setPodName(instanceEntity.getName());
        appPodTemplateVI.setPodIp(instanceEntity.getSlotIp());
        appPodTemplateVI.setPort(instanceEntity.getPort());
        appPodTemplateVI.setNamespace(instanceEntity.getNamespace());
        appPodTemplateVI.setSysctls(environment.getProperty("k8s.pod.sysctls", ""));
        appPodTemplateVI.getPodHttpReadinessProbeVI().setPort(instanceEntity.getPort());

        k8sClient.deployAppPod(appPodTemplateVI);
    }

    public void updateContainer(String dockerUrl, InstanceEntity instanceEntity) throws ApiException {
        String env = instanceEntity.getEnv();
        String zone = instanceEntity.getZone();

        K8sClient k8sClient = remoteManager.getK8sClientByZone(env, zone);

        k8sClient.updateAppPod(dockerUrl, instanceEntity.getNamespace(), instanceEntity.getName(), instanceEntity.getImage());
    }


    public void stopContainer(InstanceEntity instanceEntity) throws ApiException {
        String env = instanceEntity.getEnv();
        String zone = instanceEntity.getZone();

        K8sClient k8sClient = remoteManager.getK8sClientByZone(env, zone);

        k8sClient.deleteAppPod(instanceEntity.getNamespace(), instanceEntity.getName());
    }

    public void forceStopContainer(InstanceEntity instanceEntity) throws ApiException {
        String env = instanceEntity.getEnv();
        String zone = instanceEntity.getZone();

        K8sClient k8sClient = remoteManager.getK8sClientByZone(env, zone);

        k8sClient.forceDeleteAppPod(instanceEntity.getNamespace(), instanceEntity.getName());
    }

    public void waitUntilSuccOrTimeout(InstanceEntity instanceEntity, long timeoutMilliSecs) throws ApiException {
        String env = instanceEntity.getEnv();
        String zone = instanceEntity.getZone();

        K8sClient k8sClient = remoteManager.getK8sClientByZone(env, zone);

        long startTime = System.currentTimeMillis();

        int successIndex = 0;
        for (int i = successIndex; i < 1 && ((System.currentTimeMillis() - startTime) < timeoutMilliSecs); ) {
            Map<String, String> labelSelector = new HashMap<>();
            labelSelector.put("instance", instanceEntity.getName());

            V1PodList v1PodList = k8sClient.queryAppPods(instanceEntity.getNamespace(), labelSelector);

            if (v1PodList.getItems().size() < 1) {
                throw BaseException.newException(MessageType.ERROR,
                        "没有查到k8s对应的Pod: instance=%s", instanceEntity.getName());
            }

            V1Pod v1Pod = v1PodList.getItems().get(0);
            if (v1Pod.getStatus() == null || v1Pod.getStatus().getContainerStatuses() == null) {
                try {
                    log.info("获取Pod状态为空, 等待1s, instance={}", instanceEntity.getName());
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            } else {
                V1ContainerStatus v1ContainerStatus = v1Pod.getStatus().getContainerStatuses().get(0);

                if (v1ContainerStatus.getState().getRunning() != null) {
                    successIndex = i + 1;
                    i++;
                    if (!instanceEntity.getSlotIp().equals(v1Pod.getStatus().getPodIP())) {
                        throw BaseException.newException(MessageType.ERROR,
                                "IP不一致: 实际IP=%s, 期望IP=%s, instance=%s",
                                v1Pod.getStatus().getPodIP(),
                                instanceEntity.getSlotIp(),
                                instanceEntity.getName());
                    }
                } else if (v1ContainerStatus.getState().getTerminated() != null) {
                    throw BaseException.newException(MessageType.ERROR,
                            "k8s部署Pod失败: instance=%s, reason=%s, message=%s",
                            instanceEntity.getName(),
                            v1ContainerStatus.getState().getTerminated().getReason(),
                            v1ContainerStatus.getState().getTerminated().getMessage());
                } else if (v1ContainerStatus.getState().getWaiting() != null) {
                    try {
                        log.info("获取Pod状态=waiting, 等待1s, instance={}", instanceEntity.getName());
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    throw BaseException.newException(MessageType.ERROR,
                            "k8s部署Pod失败: instance=%s, reason=%s",
                            instanceEntity.getName(), "获取容器状态失败");
                }
            }
        }

        if (successIndex < 1) {
            throw BaseException.newException(MessageType.ERROR, "等待任务完成超时: instance=%s", instanceEntity.getName());
        }
    }

    public void waitUntilDeleteOrTimeout(InstanceEntity instanceEntity, long timeoutMilliSecs) throws ApiException {
        String env = instanceEntity.getEnv();
        String zone = instanceEntity.getZone();

        K8sClient k8sClient = remoteManager.getK8sClientByZone(env, zone);

        long startTime = System.currentTimeMillis();

        int successIndex = 0;
        for (int i = successIndex; i < 1 && ((System.currentTimeMillis() - startTime) < timeoutMilliSecs); ) {

            Map<String, String> labelSelector = new HashMap<>();
            labelSelector.put("instance", instanceEntity.getName());

            V1PodList v1PodList = k8sClient.queryAppPods(instanceEntity.getNamespace(), labelSelector);

            if (v1PodList.getItems().size() < 1) {
                log.info("Pod删除成功, instance={}", instanceEntity.getName());
                successIndex = i + 1;
                i++;
            } else {
                try {
                    log.info("Pod还未删除, 等待1s, instance={}, hostIp={}", instanceEntity.getName(), v1PodList.getItems().get(0).getStatus().getHostIP());
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }

        if (successIndex < 1) {
            throw BaseException.newException(MessageType.ERROR, "等待任务完成超时: instance=%s", instanceEntity.getName());
        }
    }

    /**
     * 查询指定实例名的容器
     * @param env
     * @param namespace
     * @param instanceName
     * @return
     * @throws ApiException
     */
    public V1PodList getContainersByInstanceName(String env, String namespace, String instanceName, String zone) throws ApiException {

        K8sClient k8sClient = remoteManager.getK8sClientByZone(env, zone);

        Map<String, String> labelSelector = new HashMap<>();
        labelSelector.put("instance", instanceName);

        V1PodList v1PodList = k8sClient.queryAppPods(namespace, labelSelector);
        return v1PodList;
    }

    /**
     * 查询指定物理机IP的容器
     * @param env
     * @param hostIp
     * @return
     * @throws ApiException
     */
    public V1PodList getContainersByHostIp(String env, String hostIp) throws ApiException {
        List<K8sClient> k8sClients = remoteManager.getK8sClientsByEnv(env);

        for (K8sClient k8sClient : k8sClients) {
            Map<String, String> fieldSelector = new HashMap<>();
            fieldSelector.put("spec.nodeName", hostIp);

            V1PodList v1PodList = k8sClient.queryAllPods(fieldSelector);
            if (v1PodList.getItems().size() > 0) {
                return v1PodList;
            }
        }

        return new V1PodList();
    }

    /**
     * 查询指定IP的容器
     * @param env
     * @param ip
     * @return
     * @throws ApiException
     */
    public V1PodList getContainersByPodIp(String env, String ip) throws ApiException {
        List<K8sClient> k8sClients = remoteManager.getK8sClientsByEnv(env);

        for (K8sClient k8sClient : k8sClients) {
            Map<String, String> fieldSelector = new HashMap<>();
            fieldSelector.put("status.podIP", ip);

            V1PodList v1PodList = k8sClient.queryAllPods(fieldSelector);
            if (v1PodList.getItems().size() > 0) {
                return v1PodList;
            }
        }

        return new V1PodList();
    }

    public V1PodList getContainersByZone(String env, String zone) throws ApiException {
        K8sClient k8sClient = remoteManager.getK8sClientByZone(env, zone);

        V1PodList v1PodList = k8sClient.queryAllPods();

        return v1PodList;
    }

    public String getContainerLog(InstanceEntity instanceEntity) {
        try {
            K8sClient k8sClient = remoteManager.getK8sClientByZone(instanceEntity.getEnv(), instanceEntity.getZone());

            return k8sClient.getAppPodLog(instanceEntity.getNamespace(), instanceEntity.getName());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw BaseException.newException(MessageType.ERROR, "查询容器日志失败, err=" + ex.getMessage());
        }
    }
}
