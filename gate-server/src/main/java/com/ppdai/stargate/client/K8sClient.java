package com.ppdai.stargate.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonSyntaxException;
import com.ppdai.stargate.constant.ConstantValue;
import com.ppdai.stargate.utils.NamingUtil;
import com.ppdai.stargate.vi.AppPodTemplateVI;
import com.ppdai.stargate.vi.PodHttpReadinessProbeVI;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Exec;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.auth.ApiKeyAuth;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class K8sClient {

    private String k8sApiServer;

    private String k8sBearerToken;

    private String env;

    private ApiClient k8sClient;

    private String formatContainerName(String containerName) {
        return containerName.replaceAll("\\.", "-");
    }

    private List<V1Volume> createVolumes() {
        List<V1Volume> v1Volumes = new ArrayList<>();
        V1Volume v1Volume = new V1Volume();
        v1Volume.setName("cpuinfo");
        V1HostPathVolumeSource v1HostPathVolumeSource = new V1HostPathVolumeSource();
        v1HostPathVolumeSource.setPath("/var/lib/lxcfs/proc/cpuinfo");
        v1Volume.setHostPath(v1HostPathVolumeSource);
        v1Volumes.add(v1Volume);

        v1Volume = new V1Volume();
        v1Volume.setName("diskstats");
        v1HostPathVolumeSource = new V1HostPathVolumeSource();
        v1HostPathVolumeSource.setPath("/var/lib/lxcfs/proc/diskstats");
        v1Volume.setHostPath(v1HostPathVolumeSource);
        v1Volumes.add(v1Volume);

        v1Volume = new V1Volume();
        v1Volume.setName("meminfo");
        v1HostPathVolumeSource = new V1HostPathVolumeSource();
        v1HostPathVolumeSource.setPath("/var/lib/lxcfs/proc/meminfo");
        v1Volume.setHostPath(v1HostPathVolumeSource);
        v1Volumes.add(v1Volume);

        v1Volume = new V1Volume();
        v1Volume.setName("stat");
        v1HostPathVolumeSource = new V1HostPathVolumeSource();
        v1HostPathVolumeSource.setPath("/var/lib/lxcfs/proc/stat");
        v1Volume.setHostPath(v1HostPathVolumeSource);
        v1Volumes.add(v1Volume);

        v1Volume = new V1Volume();
        v1Volume.setName("swaps");
        v1HostPathVolumeSource = new V1HostPathVolumeSource();
        v1HostPathVolumeSource.setPath("/var/lib/lxcfs/proc/swaps");
        v1Volume.setHostPath(v1HostPathVolumeSource);
        v1Volumes.add(v1Volume);

        v1Volume = new V1Volume();
        v1Volume.setName("uptime");
        v1HostPathVolumeSource = new V1HostPathVolumeSource();
        v1HostPathVolumeSource.setPath("/var/lib/lxcfs/proc/uptime");
        v1Volume.setHostPath(v1HostPathVolumeSource);
        v1Volumes.add(v1Volume);

        v1Volume = new V1Volume();
        v1Volume.setName("localtime");
        v1HostPathVolumeSource = new V1HostPathVolumeSource();
        v1HostPathVolumeSource.setPath("/usr/share/zoneinfo/Asia/Shanghai");
        v1Volume.setHostPath(v1HostPathVolumeSource);
        v1Volumes.add(v1Volume);

        return v1Volumes;
    }

    private V1Probe createReadinessProbe(PodHttpReadinessProbeVI podHttpReadinessProbeVI) {
        V1Probe v1Probe = new V1Probe();

        V1HTTPGetAction v1HTTPGetAction = new V1HTTPGetAction();
        v1HTTPGetAction.setPath(podHttpReadinessProbeVI.getPath());
        v1HTTPGetAction.setPort(new IntOrString(podHttpReadinessProbeVI.getPort()));
        v1Probe.setHttpGet(v1HTTPGetAction);

        v1Probe.setFailureThreshold(podHttpReadinessProbeVI.getFailureThreshold());
        v1Probe.setInitialDelaySeconds(podHttpReadinessProbeVI.getInitialDelaySeconds());
        v1Probe.setPeriodSeconds(podHttpReadinessProbeVI.getPeriodSeconds());
        v1Probe.setTimeoutSeconds(podHttpReadinessProbeVI.getTimeoutSeconds());
        v1Probe.setSuccessThreshold(podHttpReadinessProbeVI.getSuccessThreshold());

        return v1Probe;
    }

    private List<V1VolumeMount> createVolumeMounts() {
        List<V1VolumeMount> volumeMounts = new ArrayList<>();

        V1VolumeMount v1VolumeMount = new V1VolumeMount();
        v1VolumeMount.setName("cpuinfo");
        v1VolumeMount.setMountPath("/proc/cpuinfo");
        volumeMounts.add(v1VolumeMount);

        v1VolumeMount = new V1VolumeMount();
        v1VolumeMount.setName("diskstats");
        v1VolumeMount.setMountPath("/proc/diskstats");
        volumeMounts.add(v1VolumeMount);

        v1VolumeMount = new V1VolumeMount();
        v1VolumeMount.setName("meminfo");
        v1VolumeMount.setMountPath("/proc/meminfo");
        volumeMounts.add(v1VolumeMount);

        v1VolumeMount = new V1VolumeMount();
        v1VolumeMount.setName("stat");
        v1VolumeMount.setMountPath("/proc/stat");
        volumeMounts.add(v1VolumeMount);

        v1VolumeMount = new V1VolumeMount();
        v1VolumeMount.setName("swaps");
        v1VolumeMount.setMountPath("/proc/swaps");
        volumeMounts.add(v1VolumeMount);

        v1VolumeMount = new V1VolumeMount();
        v1VolumeMount.setName("uptime");
        v1VolumeMount.setMountPath("/proc/uptime");
        volumeMounts.add(v1VolumeMount);

        v1VolumeMount = new V1VolumeMount();
        v1VolumeMount.setName("localtime");
        v1VolumeMount.setMountPath("/etc/localtime");
        volumeMounts.add(v1VolumeMount);

        return volumeMounts;
    }

    private V1Container createContainer(AppPodTemplateVI appPodTemplateVI) {
        V1Container v1Container = new V1Container();

        List<V1EnvVar> envs = new ArrayList<>();
        V1EnvVar v1EnvVar = new V1EnvVar();
        v1EnvVar.setName("APP_ID");
        v1EnvVar.setValue(appPodTemplateVI.getAppId());
        envs.add(v1EnvVar);

        v1EnvVar = new V1EnvVar();
        v1EnvVar.setName("APP_NAME");
        v1EnvVar.setValue(appPodTemplateVI.getAppName());
        envs.add(v1EnvVar);

        v1EnvVar = new V1EnvVar();
        v1EnvVar.setName("INSTANCE_NAME");
        v1EnvVar.setValue(appPodTemplateVI.getPodName());
        envs.add(v1EnvVar);

        v1EnvVar = new V1EnvVar();
        v1EnvVar.setName("ENV");
        if (this.env.startsWith("fat") || this.env.startsWith("lpt")) {
            v1EnvVar.setValue("fat");
        } else if (this.env.startsWith("uat")) {
            v1EnvVar.setValue("uat");
        } else {
            v1EnvVar.setValue(this.env);
        }
        envs.add(v1EnvVar);

        v1EnvVar = new V1EnvVar();
        v1EnvVar.setName("TZ");
        v1EnvVar.setValue("Asia/Shanghai");
        envs.add(v1EnvVar);

        v1EnvVar = new V1EnvVar();
        v1EnvVar.setName("LANG");
        v1EnvVar.setValue("en_US.UTF-8");
        envs.add(v1EnvVar);

        v1EnvVar = new V1EnvVar();
        v1EnvVar.setName("LANGUAGE");
        v1EnvVar.setValue("en_US:en");
        envs.add(v1EnvVar);

        v1EnvVar = new V1EnvVar();
        v1EnvVar.setName("LC_ALL");
        v1EnvVar.setValue("en_US.UTF-8");
        envs.add(v1EnvVar);

        String envVars = appPodTemplateVI.getEnvJson();
        JSONObject jsonObject = JSON.parseObject(envVars);
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            v1EnvVar = new V1EnvVar();
            v1EnvVar.setName(entry.getKey());
            v1EnvVar.setValue(entry.getValue().toString());
            envs.add(v1EnvVar);
        }

        StringBuilder javaOpts = new StringBuilder();
        if (!StringUtils.isEmpty(appPodTemplateVI.getK8sQuotaVO().getJavaOpts())) {
            javaOpts.append(appPodTemplateVI.getK8sQuotaVO().getJavaOpts());
        }

        if (javaOpts.length() > 0) {
            v1EnvVar = new V1EnvVar();
            v1EnvVar.setName("JAVA_TOOL_OPTIONS");
            v1EnvVar.setValue(javaOpts.toString());
            envs.add(v1EnvVar);
        }

        v1Container.setEnv(envs);

        V1ResourceRequirements v1ResourceRequirements = new V1ResourceRequirements();
        if (this.env.equalsIgnoreCase("pro")) {
            v1ResourceRequirements.putLimitsItem("memory", appPodTemplateVI.getK8sQuotaVO().getLimitMemory());
            v1ResourceRequirements.putLimitsItem("cpu", appPodTemplateVI.getK8sQuotaVO().getLimitCpu());
            v1ResourceRequirements.putRequestsItem("cpu", appPodTemplateVI.getK8sQuotaVO().getRequestCpu());

            if ((appPodTemplateVI.getFlags() & ConstantValue.MEM_OVERSUBSCRIBE_FLAG) != 0) {
                v1ResourceRequirements.putRequestsItem("memory", appPodTemplateVI.getK8sQuotaVO().getRequestMemory());
            } else {
                v1ResourceRequirements.putRequestsItem("memory", appPodTemplateVI.getK8sQuotaVO().getLimitMemory());
            }
        } else {
            v1ResourceRequirements.putRequestsItem("memory", appPodTemplateVI.getK8sQuotaVO().getRequestMemory());
            v1ResourceRequirements.putRequestsItem("cpu", appPodTemplateVI.getK8sQuotaVO().getRequestCpu());
            v1ResourceRequirements.putLimitsItem("memory", appPodTemplateVI.getK8sQuotaVO().getLimitMemory());
            v1ResourceRequirements.putLimitsItem("cpu", appPodTemplateVI.getK8sQuotaVO().getLimitCpu());
        }

        v1Container.setResources(v1ResourceRequirements);
        v1Container.setImage(appPodTemplateVI.getImage());
        v1Container.setImagePullPolicy("IfNotPresent");
        v1Container.setName(formatContainerName(appPodTemplateVI.getAppName()));

        v1Container.setVolumeMounts(createVolumeMounts());

        v1Container.setReadinessProbe(createReadinessProbe(appPodTemplateVI.getPodHttpReadinessProbeVI()));

        return v1Container;
    }

    private V1Pod createPod(AppPodTemplateVI appPodTemplateVI) {
        V1Pod v1Pod = new V1Pod();
        v1Pod.setApiVersion("v1");
        v1Pod.setKind("Pod");

        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(appPodTemplateVI.getPodName());

        Map<String, String> labels = new HashMap<>();
        labels.put("app", appPodTemplateVI.getAppName());
        labels.put("appid", appPodTemplateVI.getAppId());
        labels.put("instance", appPodTemplateVI.getPodName());
        labels.put("ip", appPodTemplateVI.getPodIp());

        v1ObjectMeta.setLabels(labels);
        v1Pod.setMetadata(v1ObjectMeta);

        V1PodSpec v1PodSpec = new V1PodSpec();
        v1PodSpec.setHostname(appPodTemplateVI.getPodName().replaceAll("\\.", ""));
        v1PodSpec.setPriorityClassName(appPodTemplateVI.getK8sQuotaVO().getScope());

        if (StringUtils.isEmpty(appPodTemplateVI.getDns())) {
            v1PodSpec.setDnsPolicy("Default");
        } else {
            v1PodSpec.setDnsPolicy("None");
            V1PodDNSConfig v1PodDNSConfig = new V1PodDNSConfig();
            v1PodDNSConfig.setNameservers(Arrays.asList(appPodTemplateVI.getDns()));
            v1PodSpec.setDnsConfig(v1PodDNSConfig);
        }

        v1PodSpec.setVolumes(createVolumes());

        V1LocalObjectReference v1LocalObjectReference = new V1LocalObjectReference();
        v1LocalObjectReference.setName("dockeryardkey");
        v1PodSpec.addImagePullSecretsItem(v1LocalObjectReference);

        List<V1Container> containers = new ArrayList<>();
        containers.add(createContainer(appPodTemplateVI));

        v1PodSpec.setContainers(containers);
        v1PodSpec.setRestartPolicy("Always");

        V1PodSecurityContext v1PodSecurityContext = new V1PodSecurityContext();
        List<V1Sysctl> v1Sysctls = new ArrayList<>();
        for (String sysctl : appPodTemplateVI.getSysctls().split(",")) {
            V1Sysctl v1Sysctl = new V1Sysctl();
            String[] pair = sysctl.trim().split("=");
            if (pair.length > 1) {
                v1Sysctl.setName(pair[0]);
                v1Sysctl.setValue(pair[1]);
                v1Sysctls.add(v1Sysctl);
            }
        }

        v1PodSecurityContext.setSysctls(v1Sysctls);
        v1PodSpec.setSecurityContext(v1PodSecurityContext);

        List<V1HostAlias> hostAliases = new ArrayList<>();
        V1HostAlias v1HostAlias = new V1HostAlias();
        v1HostAlias.setIp("127.0.0.1");
        String[] hostNames1 = new String[]{"localhost.localdomain", "localhost4", "localhost4.localdomain4"};
        v1HostAlias.setHostnames(Arrays.asList(hostNames1));
        hostAliases.add(v1HostAlias);

        v1HostAlias = new V1HostAlias();
        v1HostAlias.setIp("::1");
        String[] hostNames2 = new String[]{"localhost.localdomain", "localhost6", "localhost6.localdomain6"};
        v1HostAlias.setHostnames(Arrays.asList(hostNames2));
        hostAliases.add(v1HostAlias);

        v1PodSpec.setHostAliases(hostAliases);

        v1Pod.setSpec(v1PodSpec);

        return v1Pod;
    }

    public K8sClient(String env, String k8sApiServer, String k8sBearerToken) {
        this.env = env.toLowerCase();
        this.k8sApiServer = k8sApiServer;
        this.k8sBearerToken = k8sBearerToken;

        k8sClient = new ApiClient();
        k8sClient.setBasePath(k8sApiServer);

        ApiKeyAuth bearerToken = (ApiKeyAuth) k8sClient.getAuthentication("BearerToken");
        bearerToken.setApiKey(k8sBearerToken);
    }

    /**
     * 创建namespace
     * @param namespace
     * @throws ApiException
     */
    public void createNamespace(String namespace) throws ApiException {
        CoreV1Api coreV1Api = new CoreV1Api(k8sClient);

        V1Namespace body = new V1Namespace();
        body.setApiVersion("v1");
        body.setKind("Namespace");

        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(namespace);
        body.setMetadata(v1ObjectMeta);

        String pretty = "true";

        try {
            coreV1Api.createNamespace(body, false, pretty, null);
        } catch (ApiException e) {
            if (!e.getMessage().equals("Conflict")) {
                throw e;
            }
        }
    }

    public void createConfigMap(String namespace, String configName, Map<String, String> dataMap) throws ApiException {
        CoreV1Api coreV1Api = new CoreV1Api(k8sClient);

        V1ConfigMap body = new V1ConfigMap();
        body.setApiVersion("v1");

        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(configName);
        body.setMetadata(v1ObjectMeta);
        body.setData(dataMap);

        String pretty = "true";
        try {
            coreV1Api.createNamespacedConfigMap(namespace, body, false, pretty, null);
        } catch (ApiException e) {
            if (!e.getMessage().equals("Conflict")) {
                throw e;
            }
        }
    }

    public V1ConfigMap getConfigMap(String namespace, String configName) throws ApiException {
        CoreV1Api coreV1Api = new CoreV1Api(k8sClient);

        try {
            return coreV1Api.readNamespacedConfigMap(configName, namespace, "false", false, false);
        } catch (ApiException e) {
            if (e.getMessage().toLowerCase().contains("not found")) {
                return null;
            }

            throw e;
        }
    }

    public void deleteConfigMap(String namespace, String configName) throws ApiException {
        CoreV1Api coreV1Api = new CoreV1Api(k8sClient);

        V1DeleteOptions body = new V1DeleteOptions();
        String pretty = "false";
        Integer gracePeriodSeconds = 0;
        Boolean orphanDependents = false;
        String propagationPolicy = "Background";

        try {
            coreV1Api.deleteNamespacedConfigMap(configName, namespace, body, pretty, null,
                    gracePeriodSeconds, orphanDependents, propagationPolicy);
        } catch (ApiException e) {
            if (!e.getMessage().toLowerCase().contains("not found")) {
                throw e;
            }
        }
    }

    /**
     * 查询指定namespace的Pods
     * @param namespace
     * @return
     * @throws ApiException
     */
    public V1PodList queryAppPods(String namespace, Map<String, String> labelSelectorMap) throws ApiException {
        CoreV1Api coreV1Api = new CoreV1Api(k8sClient);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : labelSelectorMap.entrySet())
        {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        }

        String labelSelector = sb.substring(0, sb.length() - 1);
        return coreV1Api.listNamespacedPod(namespace, false, "false", null, null,
                labelSelector, 9999, null, -1, false);

    }

    /**
     * 查询所有namespace的Pods
     * @param fieldSelectorMap
     * @return
     * @throws ApiException
     */
    public V1PodList queryAllPods(Map<String, String> fieldSelectorMap) throws ApiException {
        CoreV1Api coreV1Api = new CoreV1Api(k8sClient);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : fieldSelectorMap.entrySet())
        {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        }

        String fieldSelector = sb.substring(0, sb.length() - 1);
        return coreV1Api.listPodForAllNamespaces(null, fieldSelector, true,
                null, 9999, "false", null, -1, false);

    }

    public V1PodList queryAllPods() throws ApiException {
        CoreV1Api coreV1Api = new CoreV1Api(k8sClient);

        return coreV1Api.listPodForAllNamespaces(null, null, true,
                null, 9999, "false", null, -1, false);

    }

    public void deployAppPod(AppPodTemplateVI appPodTemplateVI) throws ApiException {
        try {
            CoreV1Api coreV1Api = new CoreV1Api(k8sClient);

            // 创建Pod
            V1Pod v1Pod = createPod(appPodTemplateVI);

            try {
                V1Pod result = coreV1Api.createNamespacedPod(appPodTemplateVI.getNamespace(),
                        v1Pod, false,"true", null);
                log.info("创建Pod成功, result: {}", result);
            } catch (ApiException e) {
                if (e.getMessage().equals("Conflict")) {
                    log.info("创建Pod失败, {} already exists", v1Pod.getMetadata().getName());
                } else {
                    throw e;
                }
            }

        } catch (ApiException apiException) {
            log.error("创建Pod失败, err=" + apiException.getMessage() + ", k8s response=" + apiException.getResponseBody());
            throw apiException;
        }
    }

    public void updateAppPod(String dockerUrl, String namespace, String podName, String image) throws ApiException {
        try {
            CoreV1Api coreV1Api = new CoreV1Api(k8sClient);

            V1Pod v1Pod = coreV1Api.readNamespacedPod(podName,
                    namespace,
                    "true", true, true);
            V1PodSpec spec = v1Pod.getSpec();
            List<V1Container> containers = spec.getContainers();
            List<V1Container> collect = containers.stream().map(container -> {
                String newImage = dockerUrl + "/" + image;
                container.setImage(newImage);
                return container;
            }).collect(Collectors.toList());
            spec.setContainers(collect);
            v1Pod.setSpec(spec);

            V1Pod result = coreV1Api.replaceNamespacedPod(v1Pod.getMetadata().getName(), v1Pod.getMetadata().getNamespace(), v1Pod, "true", null);
            log.info("更新Pod成功, result: {}", result);
        } catch (ApiException apiException) {
            log.error("更新Pod失败, err=" + apiException.getMessage() + ", k8s response=" + apiException.getResponseBody());
            throw apiException;
        }
    }

    public void deleteAppPod(String namespace, String podName) throws ApiException {
        CoreV1Api apiInstance = new CoreV1Api(k8sClient);

        V1DeleteOptions body = new V1DeleteOptions();
        String pretty = "false";
        Integer gracePeriodSeconds = 0;
        Boolean orphanDependents = false;
        String propagationPolicy = "Background";
        try {
            apiInstance.deleteNamespacedPod(podName,
                    namespace,
                    body, pretty, null,
                    gracePeriodSeconds, orphanDependents, propagationPolicy);
            log.info("删除Pod成功, instanceName={}", podName);

        } catch (JsonSyntaxException e) {
            if (e.getCause() instanceof IllegalStateException) {
                IllegalStateException ise = (IllegalStateException) e.getCause();
                if (ise.getMessage() != null && ise.getMessage().contains("Expected a string but was BEGIN_OBJECT"))
                    log.debug("Catching exception because of issue https://github.com/kubernetes-client/java/issues/86", e);
                else throw e;
            } else throw e;
        } catch (ApiException apiException) {
            if (!apiException.getMessage().toLowerCase().contains("not found")) {
                log.error("删除Pod失败, err=" + apiException.getMessage() + ", k8s response=" + apiException.getResponseBody());
                throw apiException;
            }
        }
    }

    public void forceDeleteAppPod(String namespace, String podName) throws ApiException {
        CoreV1Api apiInstance = new CoreV1Api(k8sClient);

        V1DeleteOptions body = new V1DeleteOptions();
        body.setGracePeriodSeconds(0l);
        String pretty = "false";
        Integer gracePeriodSeconds = 0;
        Boolean orphanDependents = false;
        String propagationPolicy = "Background";
        try {
            apiInstance.deleteNamespacedPod(podName,
                    namespace,
                    body, pretty, null,
                    gracePeriodSeconds, orphanDependents, propagationPolicy);
            log.info("删除Pod成功, instanceName={}", podName);

        } catch (JsonSyntaxException e) {
            if (e.getCause() instanceof IllegalStateException) {
                IllegalStateException ise = (IllegalStateException) e.getCause();
                if (ise.getMessage() != null && ise.getMessage().contains("Expected a string but was BEGIN_OBJECT"))
                    log.debug("Catching exception because of issue https://github.com/kubernetes-client/java/issues/86", e);
                else throw e;
            } else throw e;
        } catch (ApiException apiException) {
            if (!apiException.getMessage().toLowerCase().contains("not found")) {
                log.error("删除Pod失败, err=" + apiException.getMessage() + ", k8s response=" + apiException.getResponseBody());
                throw apiException;
            }
        }
    }

    public String execCommand(String pod, String namespace, String[] commands) throws IOException, ApiException, InterruptedException {
        Exec exec = new Exec(k8sClient);
        final Process proc =
                exec.exec(
                        namespace,
                        pod,
                        commands,
                        false,
                        false);

        final StringBuilder stdoutBuilder = new StringBuilder();
        final StringBuilder stderrBuilder = new StringBuilder();

        Thread stdout =
                new Thread(
                        new Runnable() {
                            public void run() {
                                try (Reader reader = new BufferedReader(new InputStreamReader
                                        (proc.getInputStream(), Charset.forName(StandardCharsets.UTF_8.name())))) {
                                    int c = 0;
                                    while ((c = reader.read()) != -1) {
                                        stdoutBuilder.append((char) c);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        });
        stdout.start();

        Thread stderr =
                new Thread(
                        new Runnable() {
                            public void run() {
                                try (Reader reader = new BufferedReader(new InputStreamReader
                                        (proc.getErrorStream(), Charset.forName(StandardCharsets.UTF_8.name())))) {
                                    int c = 0;
                                    while ((c = reader.read()) != -1) {
                                        stderrBuilder.append((char) c);
                                    }
                                } catch (IOException e) {

                                }
                            }
                        });
        stderr.start();

        proc.waitFor(20, TimeUnit.SECONDS);

        stdout.join(1000);
        stderr.join(1000);

        proc.destroy();
        return stdoutBuilder.toString() + "\n" + stderrBuilder.toString();
    }

    public V1Node getNodeByIp(String hostIp) throws ApiException {
        CoreV1Api coreV1Api = new CoreV1Api(k8sClient);

        V1NodeList v1NodeList = coreV1Api.listNode(true, "false", null, null,
                "kubernetes.io/hostname=" + hostIp, 1, null, -1, false);

        if (v1NodeList.getItems().size() > 0) {
            return v1NodeList.getItems().get(0);
        }

        return null;
    }

    /**
     * 获取Pod里面容器的日志
     * @param namespace
     * @param instanceName
     * @return
     */
    public String getAppPodLog(String namespace, String instanceName) throws ApiException {

        CoreV1Api apiInstance = new CoreV1Api(k8sClient);
        String name = instanceName;
        String container = null;
        Boolean follow = false;
        Integer limitBytes = 1 * 1024 * 1024;
        String pretty = "true";
        Boolean previous = false;
        Integer sinceSeconds = null;
        Integer tailLines = 5000;
        Boolean timestamps = false;
        try {
            return apiInstance.readNamespacedPodLog(name, namespace, container, follow, limitBytes,
                    pretty, previous, sinceSeconds, tailLines, timestamps);
        } catch (ApiException apiException) {
            log.error("查询Pod日志失败, err=" + apiException.getMessage() + ", k8s response=" + apiException.getResponseBody());
            throw apiException;
        }
    }

    public void createService(String appName, String namespace) throws ApiException {
        CoreV1Api apiInstance = new CoreV1Api(k8sClient);

        V1Service v1Service = new V1Service();
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(NamingUtil.getServiceFromAppName(appName));
        v1Service.setMetadata(v1ObjectMeta);

        v1Service.setKind("Service");
        v1Service.setApiVersion("v1");

        V1ServiceSpec v1ServiceSpec = new V1ServiceSpec();
        Map<String, String> selector = new HashMap<>();
        selector.put("app", appName);
        v1ServiceSpec.selector(selector);

        V1ServicePort v1ServicePort = new V1ServicePort();
        v1ServicePort.setProtocol("TCP");
        v1ServicePort.setPort(8080);
        v1ServicePort.setTargetPort(new IntOrString(8080));
        v1ServiceSpec.setPorts(Arrays.asList(v1ServicePort));

        v1Service.setSpec(v1ServiceSpec);

        try {
            apiInstance.createNamespacedService(namespace, v1Service, false,"false", null);
        } catch (ApiException apiException) {
            if (!apiException.getMessage().equals("Conflict")) {
                log.error("创建Service失败, err=" + apiException.getMessage() + ", k8s response=" + apiException.getResponseBody());
                throw apiException;
            }
        }
    }

    public String getApiServer() {
        return k8sApiServer;
    }
}
