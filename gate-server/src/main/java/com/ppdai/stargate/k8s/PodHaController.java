package com.ppdai.stargate.k8s;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ppdai.atlas.client.model.ZoneDto;
import com.ppdai.stargate.client.JsonHttpClient;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.manager.TaskManager;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.*;
import com.ppdai.stargate.utils.NamingUtil;
import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.EndpointSubset;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.client.*;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ConfigMap;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PodHaController {

    private static Logger log = LoggerFactory.getLogger(PodHaController.class);

    private volatile boolean watchClosed = false;

    private String token;
    private ZoneDto zoneDto;
    private long eventPollingSecs;
    private KubernetesClient client;
    private Set<String> excludeNamespaces;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    Cache<String, String> notReadyAddressCache;
    Cache<String, String> readyAddressCache;

    private AppService appService;
    private InstanceJobService instanceJobService;
    private InstanceService instanceService;
    private ContainerService containerService;

    private TaskManager taskManager;

    private JsonHttpClient hcHttpClient;

    private OkHttpClient hcProxyClient;

    private Environment environment;

    private boolean hcCheck(String url) {
        try {
            hcHttpClient.get(url);
            return true;
        } catch (Exception ex) {
            log.warn("msg=实例健康检测失败: url=" + url + ", err=" + ex.getMessage(), ex);
            return false;
        }
    }

    public boolean hcCheckByProxy(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = hcProxyClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.warn("msg=实例健康检测失败: url=" + url + ", code=" + response.code() + ", status=" + response.message());
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.warn("msg=实例健康检测失败: url=" + url + ", err=" + ex.getMessage(), ex);
            return false;
        }
    }

    private Watcher<Endpoints> createWatcher() {
        return new Watcher<Endpoints>() {
            @Override
            public void eventReceived(Action action, Endpoints endpoints) {
                processEndpoints(endpoints);
            }

            @Override
            public void onClose(KubernetesClientException e) {
                try {
                    log.error("事件监听时与apiserver发生链接中断, env=" + zoneDto.getEnvName() + ", zone=" + zoneDto.getName()
                            + ", apiserver=" + zoneDto.getK8s() + ", err=" + e.getMessage(), e);
                    watchClosed = true;
                } catch (Exception ex) {
                    log.error("在onClose中发生未知异常, err=" + ex.getMessage(), ex);
                }
            }
        };
    }

    private void processEndpoints(Endpoints endpoints) {
        if (excludeNamespaces.contains(endpoints.getMetadata().getNamespace())) {
            return;
        }

        for (EndpointSubset endpointSubset : endpoints.getSubsets()) {
            // 对不可用的实例做处理
            for (EndpointAddress endpointAddress : endpointSubset.getNotReadyAddresses()) {
                // 根据endpointAddress里面的IP从实例表中查询出实例
                // 如果实例被人工拉入流量，则新增下线原有实例流量任务、添加新实例任务
                processNotReadyAddress(endpoints.getMetadata().getName(), endpointAddress);
            }

            // 对可用的实例做处理
            for (EndpointAddress endpointAddress : endpointSubset.getAddresses()) {
                // 根据endpointAddress里面的IP从实例表中查询出实例
                // 如果实例被人工拉入流量，则新增上线该实例流量任务、删除原有实例任务
                processReadyAddress(endpoints.getMetadata().getName(), endpointAddress);
            }
        }
    }

    /**
     * 处理不带发布组的不可用实例
     * @param instanceEntity
     */
    private void processNotReadyAddressSingle(InstanceEntity instanceEntity) throws ApiException {
        /**
         * 非生产环境的实例若流量未拉入则不处理
         */
        if (!instanceEntity.getEnv().equals("pro") && instanceEntity.getHasPulledIn() == false) {
            log.warn("msg=非生产实例未拉入流量忽略处理, ready=false, env={}, zone={}, instance={}, hasPulledIn={}",
                    zoneDto.getEnvName(), zoneDto.getName(), instanceEntity.getName(), instanceEntity.getHasPulledIn());
            return;
        }

        /**
         * 线下环境多zone会复用现有的k8s集群，把不属于自己环境的实例过滤掉
         */
        if (!instanceEntity.getEnv().equals(zoneDto.getEnvName())) {
            log.warn("msg=实例不属于当前环境忽略处理, ready=false, env={}, zone={}, instance={}, ip={}, instanceEnv={}",
                    zoneDto.getEnvName(), zoneDto.getName(),
                    instanceEntity.getName(), instanceEntity.getSlotIp(), instanceEntity.getEnv());
            return;
        }

        ApplicationEntity applicationEntity = appService.getAppByCmdbId(instanceEntity.getAppId());
        if (applicationEntity == null) {
            log.error("msg=实例未找到应用, ready=false, env={}, zone={}, appId={}, enableHa=false, instance={}, ip={}",
                        zoneDto.getEnvName(), zoneDto.getName(), instanceEntity.getAppId(),
                    instanceEntity.getName(), instanceEntity.getSlotIp());
            return;
        }

        /**
         * 应用没有启用高可用，不处理
         */
        if (!applicationEntity.getEnableHa()) {
            log.info("msg=应用未开启高可用忽略处理, ready=false, env={}, zone={}, appName={}, enableHa=false, instance={}, ip={}",
                    zoneDto.getEnvName(), zoneDto.getName(), applicationEntity.getName(),
                    instanceEntity.getName(), instanceEntity.getSlotIp());
            return;
        }

        /**
         * 实例健康检测OK，不处理。由于kubelet停止后也会产生notready的事件，所以忽略由于kubelet停止的迁移
         */
        boolean hcCheckResult;
        String url = "http://" + instanceEntity.getSlotIp() + ":" + instanceEntity.getPort().toString() + "/hs";
        if (instanceEntity.getEnv().equals("pro") || instanceEntity.getEnv().equals("pre")) {
            hcCheckResult = hcCheck(url);
        } else {
            hcCheckResult = hcCheckByProxy(url);
        }
        if (hcCheckResult) {
            log.info("msg=实例健康检测OK无需做迁移, ready=false, env={}, zone={}, instance={}, ip={}, port={}",
                    zoneDto.getEnvName(), zoneDto.getName(), instanceEntity.getName(), instanceEntity.getSlotIp(), instanceEntity.getPort());
            return;
        }

        if (taskManager.hasInProcessTasksByInstance(instanceEntity.getId())) {
            log.info("msg=实例当前有任务正在处理, ready=false, env={}, zone={}, instance={}, ip={}",
                    zoneDto.getEnvName(), zoneDto.getName(), instanceEntity.getName(), instanceEntity.getSlotIp());
            return;
        }

        /**
         * 忽略一定时间内的重复事件
         */
        synchronized (notReadyAddressCache) {
            String v = notReadyAddressCache.getIfPresent(instanceEntity.getName());

            if (!StringUtils.isEmpty(v)) {
                log.info("msg=缓存中已经有值暂不处理, ready=false, env={}, zone={}, instance={}, ip={}",
                        zoneDto.getEnvName(), zoneDto.getName(), instanceEntity.getName(), instanceEntity.getSlotIp());
                return;
            }

            notReadyAddressCache.put(instanceEntity.getName(), instanceEntity.getName());
        }

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put(NamingUtil.NOT_READY_FLOW, instanceEntity.getHasPulledIn().toString());

        // 创建configmap
        containerService.createConfigMap(zoneDto.getEnvName(), zoneDto.getName(), instanceEntity.getNamespace(), instanceEntity.getName(), dataMap);

        JobInfo jobInfo = instanceJobService.recoverInstance1Step(instanceEntity);

        log.info("msg=添加恢复实例上任务, ready=false, env={}, zone={}, instance={}, ip={}, jobId={}",
                zoneDto.getEnvName(), zoneDto.getName(), instanceEntity.getName(), instanceEntity.getSlotIp(), jobInfo.getId());
    }

    /**
     * 处理失败实例
     * @param serviceName
     * @param endpointAddress
     */
    private void processNotReadyAddress(String serviceName, EndpointAddress endpointAddress) {
        try {

            int silentSecs = Integer.parseInt(environment.getProperty("stargate.podHa.silentSecs", "300"));
            int maxRunningJobs = Integer.parseInt(environment.getProperty("stargate.podHa.maxRunningJobs", "5"));

            String instanceName = endpointAddress.getTargetRef().getName();

            log.info("msg=开始处理不可用实例, ready=false, env={}, zone={}, service={}, instance={}, ip={}",
                    zoneDto.getEnvName(), zoneDto.getName(), serviceName, instanceName, endpointAddress.getIp());

            InstanceEntity instanceEntity = instanceService.findByName(instanceName);
            if (instanceEntity == null) {
                    log.warn("msg=没有找到对应的实例, ready=false, env={}, zone={}, instance={}",
                            zoneDto.getEnvName(), zoneDto.getName(), instanceName);
                    return;
            }

            /**
             * 线下环境多zone会复用现有的k8s集群，把不属于自己zone的实例过滤掉
             */
            if (!instanceEntity.getZone().equals(zoneDto.getName())) {
                log.warn("msg=实例不属于当前zone忽略处理, ready=false, env={}, zone={}, instance={}, ip={}, instanceZone={}, currentZone={}",
                        zoneDto.getEnvName(), zoneDto.getName(),
                        instanceEntity.getName(), instanceEntity.getSlotIp(), instanceEntity.getZone(), zoneDto.getName());
                return;
            }

            /**
             * 在静默期的实例不处理，静默期是用户操作完实例之后的一段时间
             */
            long timeMsPassed = System.currentTimeMillis() - instanceEntity.getUpdateTime().getTime();
            if (timeMsPassed < (silentSecs * 1000)) {
                log.info("msg=距实例上次更新时间小于{}秒暂不处理, ready=false, env={}, zone={}, instance={}",
                        silentSecs, zoneDto.getEnvName(), zoneDto.getName(), instanceName);
                return;
            }

            processNotReadyAddressSingle(instanceEntity);

        } catch (Exception ex) {
            log.error("处理不可用节点失败, err=" + ex.getMessage(), ex);
        } finally {
        }
    }

    /**
     * 处理不带发布组的可用实例
     * @param instanceEntity
     */
    private void processReadyAddressSingle(InstanceEntity instanceEntity) throws ApiException {
        /**
         * 线下环境多zone会复用现有的k8s集群，把不属于自己环境的实例过滤掉
         */
        if (!instanceEntity.getEnv().equals(zoneDto.getEnvName())) {
            log.warn("msg=实例不属于当前环境忽略处理, ready=false, env={}, zone={}, instance={}, ip={}, instanceEnv={}",
                    zoneDto.getEnvName(), zoneDto.getName(),
                    instanceEntity.getName(), instanceEntity.getSlotIp(), instanceEntity.getEnv());
            return;
        }

        ApplicationEntity applicationEntity = appService.getAppByCmdbId(instanceEntity.getAppId());
        if (applicationEntity == null) {
            log.error("msg=实例未找到应用, ready=false, env={}, zone={}, appId={}, enableHa=false, instance={}, ip={}",
                    zoneDto.getEnvName(), zoneDto.getName(), instanceEntity.getAppId(),
                    instanceEntity.getName(), instanceEntity.getSlotIp());
            return;
        }

        /**
         * 应用没有启用高可用，不处理
         */
//        if (!applicationEntity.getEnableHa()) {
//            log.info("msg=应用未开启高可用忽略处理, ready=false, env={}, zone={}, appName={}, enableHa=false, instance={}, ip={}",
//                    zoneDto.getEnvName(), zoneDto.getName(), applicationEntity.getName(),
//                    instanceEntity.getName(), instanceEntity.getSlotIp());
//            return;
//        }

        if (taskManager.hasInProcessTasksByInstance(instanceEntity.getId())) {
            log.info("msg=实例当前有任务正在处理, ready=false, env={}, zone={}, instance={}, ip={}",
                    zoneDto.getEnvName(), zoneDto.getName(), instanceEntity.getName(), instanceEntity.getSlotIp());
            return;
        }

        V1ConfigMap v1ConfigMap = containerService.getConfigMap(zoneDto.getEnvName(), zoneDto.getName(),
                instanceEntity.getNamespace(), instanceEntity.getName());
        if (v1ConfigMap == null) {
            log.info("msg=可用实例不是恢复实例忽略处理, ready=true, env={}, zone={}, instance={}, ip={}",
                    zoneDto.getEnvName(), zoneDto.getName(), instanceEntity.getName(), instanceEntity.getSlotIp());
            return;
        }

        boolean isPullIn = Boolean.valueOf(v1ConfigMap.getData().get(NamingUtil.NOT_READY_FLOW));

        /**
         * 忽略一定时间内的重复事件
         */
        synchronized (readyAddressCache) {
            String v = readyAddressCache.getIfPresent(instanceEntity.getName());

            if (!StringUtils.isEmpty(v)) {
                log.info("msg=缓存中已经有值暂不处理, ready=true, env={}, zone={},  instance={}, ip={}",
                        zoneDto.getEnvName(), zoneDto.getName(), instanceEntity.getName(), instanceEntity.getSlotIp());
                return;
            }

            readyAddressCache.put(instanceEntity.getName(), instanceEntity.getName());
        }

        JobInfo jobInfo = null;
        if (isPullIn) {
            jobInfo = instanceJobService.recoverInstance2Step(instanceEntity);
        }

        containerService.deleteConfigMap(zoneDto.getEnvName(), zoneDto.getName(),
                instanceEntity.getNamespace(), instanceEntity.getName());

        log.info("msg=添加恢复实例下任务, ready=false, env={}, zone={}, instance={}, ip={},  isPullIn={}, jobId={}",
                zoneDto.getEnvName(), zoneDto.getName(), instanceEntity.getName(), instanceEntity.getSlotIp(),
                isPullIn, (jobInfo != null ? jobInfo.getId() : "null"));
    }

    /**
     * 处理成功实例
     * @param serviceName
     * @param endpointAddress
     */
    private void processReadyAddress(String serviceName, EndpointAddress endpointAddress) {
        try {
            String instanceName = endpointAddress.getTargetRef().getName();

            log.info("msg=开始处理可用实例, ready=true, env={}, zone={}, service={}, instance={}, ip={}",
                    zoneDto.getEnvName(), zoneDto.getName(), serviceName, instanceName, endpointAddress.getIp());

            InstanceEntity instanceEntity = instanceService.findByName(instanceName);
            if (instanceEntity == null) {
                log.warn("msg=没有找到对应的实例, ready=true, env={}, zone={}, instance={}",
                        zoneDto.getEnvName(), zoneDto.getName(), instanceName);
                return;
            }

            /**
             * 线下环境多zone会复用现有的k8s集群，把不属于自己zone的实例过滤掉
             */
            if (!instanceEntity.getZone().equals(zoneDto.getName())) {
                log.warn("msg=实例不属于当前zone忽略处理, ready=true, env={}, zone={}, instance={}, ip={}, instanceZone={}, currentZone={}",
                        zoneDto.getEnvName(), zoneDto.getName(),
                        instanceName, instanceEntity.getSlotIp(), instanceEntity.getZone(), zoneDto.getName());
                return;
            }

            processReadyAddressSingle(instanceEntity);

        } catch (Exception ex) {
            log.error("处理可用节点失败, err=" + ex.getMessage(), ex);
        } finally {
        }
    }

    public PodHaController(String token,
                           ZoneDto zoneDto,
                           JsonHttpClient hcHttpClient,
                           long eventCacheSecs,
                           long eventPollingSecs,
                           AppService appService,
                           InstanceService instanceService,
                           InstanceJobService instanceJobService,
                           ContainerService containerService,
                           TaskManager taskManager,
                           Environment environment) {
        this.token = token;
        this.zoneDto = zoneDto;
        this.hcHttpClient = hcHttpClient;
        this.eventPollingSecs = eventPollingSecs;
        this.appService = appService;
        this.instanceService = instanceService;
        this.instanceJobService = instanceJobService;
        this.containerService = containerService;
        this.taskManager = taskManager;
        this.environment = environment;

        Config config = new ConfigBuilder().withMasterUrl(zoneDto.getK8s()).build();
        this.client = new DefaultKubernetesClient(config);

        this.excludeNamespaces = new HashSet<>(Arrays.asList("default", "kube-public", "kube-system"));

        notReadyAddressCache = CacheBuilder.newBuilder()
                .expireAfterWrite(eventCacheSecs, TimeUnit.SECONDS)
                .build();

        readyAddressCache = CacheBuilder.newBuilder()
                .expireAfterWrite(eventCacheSecs, TimeUnit.SECONDS)
                .build();

        String proxyIp = environment.getProperty("stargate.podHa.hcProxy", "127.0.0.1");
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, 80));
        this.hcProxyClient = new OkHttpClient.Builder().connectTimeout(5000, TimeUnit.MILLISECONDS)
                .proxy(proxy).readTimeout(5000, TimeUnit.MILLISECONDS).build();
    }

    public void startEventTrigger() {
        watchClosed = false;
        client.endpoints().watch(createWatcher());

        log.info("eventTrigger started, env={}, zone={}, apiserver={}", zoneDto.getEnvName(), zoneDto.getName() ,zoneDto.getK8s());
    }

    public void startEventPolling() {
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("开使轮训endpoints事件, env={}, zone={}, apiserver={}", zoneDto.getEnvName(), zoneDto.getName(), zoneDto.getK8s());
                    EndpointsList endpointsList = client.endpoints().list();
                    for (Endpoints endpoints : endpointsList.getItems()) {
                        processEndpoints(endpoints);
                    }
                } catch (Exception ex) {
                    log.error("轮训endpoints事件发生异常, env=" + zoneDto.getEnvName()
                            + ", zone=" + zoneDto.getName() + ", apiserver=" + zoneDto.getK8s() + ", err=" + ex.getMessage(), ex);
                } finally {
                    log.info("结束轮训endpoints事件, env={}, zone={}, apiserver={}", zoneDto.getEnvName(), zoneDto.getName(), zoneDto.getK8s());
                }
            }
        }, eventPollingSecs, eventPollingSecs, TimeUnit.SECONDS);

        log.info("eventPolling started, env={}, zone={}, apiserver={}", zoneDto.getEnvName(), zoneDto.getName(), zoneDto.getK8s());
    }

    public void stopEventTrigger() {
        try {
            Client baseClient = client;
            baseClient.close();

            log.info("eventTrigger stopped, env={}, zone={}, apiserver={}", zoneDto.getEnvName(), zoneDto.getName(), zoneDto.getK8s());
        } catch (Exception ex) {
            log.error("eventTrigger停止失败, env=" + zoneDto.getEnvName()
                    + ", zone=" + zoneDto.getName() + ", apiserver=" + zoneDto.getK8s() + ", err=" + ex.getMessage(), ex);
        }
    }

    public void stopEventPolling() {
        try {
            scheduler.shutdown();

            log.info("eventPolling stopped, env={}, zone={}, apiserver={}", zoneDto.getEnvName(), zoneDto.getName(), zoneDto.getK8s());
        } catch (Exception ex) {
            log.error("eventPolling停止失败, env=" + zoneDto.getEnvName()
                    + ", zone=" + zoneDto.getName() + ", apiserver=" + zoneDto.getK8s() + ", err=" + ex.getMessage(), ex);
        }
    }

    public boolean isWatchClosed() {
        return watchClosed;
    }

    public ZoneDto getZone() {
        return zoneDto;
    }
}
