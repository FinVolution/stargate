package com.ppdai.stargate.service;

import com.ppdai.atlas.client.model.ZoneDto;
import com.ppdai.stargate.constant.LockEnum;
import com.ppdai.stargate.k8s.ControllerFactoryService;
import com.ppdai.stargate.k8s.PodHaController;
import com.ppdai.stargate.po.GlobalLockEntity;
import com.ppdai.stargate.utils.IPUtil;
import com.ppdai.stargate.vo.EnvVO;
import io.kubernetes.client.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "stargate.podHa.enable", havingValue = "true")
@Slf4j
public class PodHaService {

    private static final long RETENTION = 20000l;

    @Autowired
    private DistLockService distLockService;

    @Autowired
    private EnvService envService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private ControllerFactoryService controllerFactoryService;

    private volatile boolean isMaster = false;

    private String node = IPUtil.getLocalIP();

    private Map<String, List<PodHaController>> podHaControllerMap = new LinkedHashMap<>();

    synchronized private void addPodHaController(String env, List<ZoneDto> zoneDtoList) throws ApiException {
        if (podHaControllerMap.containsKey(env)) {
            return;
        }

        List<PodHaController> podHaControllerList = new ArrayList<>();

        for (ZoneDto zoneDto : zoneDtoList) {
            log.info("开始添加PodHaController, env={}, zone={}, apiserver={}", zoneDto.getEnvName(), zoneDto.getName(), zoneDto.getK8s());
            try {
                PodHaController podHaController = controllerFactoryService.createPodHaController(zoneDto);
                podHaController.startEventTrigger();
                podHaController.startEventPolling();

                podHaControllerList.add(podHaController);
            } catch (Exception ex) {
                log.error("添加PodHaController失败, env=" + env + ", zone=" + zoneDto.getName()
                        + ", apiserver=" + zoneDto.getK8s() + ", err=" + ex.getMessage(), ex);
            }
        }

        if (podHaControllerList.size() > 0) {
            podHaControllerMap.put(env, podHaControllerList);
            log.info("添加PodHaController成功, env={}, PodHaController数量={}", env, podHaControllerList.size());
        }
    }

    synchronized private void removePodHaController(String env) {
        List<PodHaController> podHaControllers = podHaControllerMap.get(env);
        if (podHaControllers == null) {
            return;
        }

        for (PodHaController podHaController : podHaControllers) {
            podHaController.stopEventTrigger();
            podHaController.stopEventPolling();
        }

        podHaControllerMap.remove(env);
        log.info("删除PodHaController成功, env={}, PodHaController数量={}", env, podHaControllers.size());
    }

    synchronized private void removeAllPodHaControllers() {

        try {

            if (podHaControllerMap.isEmpty()) {
                return;
            }

            for (Map.Entry<String, List<PodHaController>> entry : podHaControllerMap.entrySet()) {
                for (PodHaController podHaController : entry.getValue()) {
                    podHaController.stopEventTrigger();
                    podHaController.stopEventPolling();
                }
            }

            podHaControllerMap.clear();
            log.info("清楚PodHaController成功");
        } catch (Exception ex) {
            log.error("清楚PodHaController失败, err=" + ex.getMessage(), ex);
        }
    }

    @Scheduled(initialDelayString = "${stargate.podHa.initialDelay}", fixedRateString = "${stargate.podHa.fixedRate}")
    public void poll() {
        try {
            GlobalLockEntity globalLockEntity = distLockService.tryLock(LockEnum.HA, node, RETENTION);

            boolean newMaster = false;
            if (globalLockEntity != null){
                newMaster = distLockService.lock(LockEnum.HA, node, globalLockEntity.getOwner(), RETENTION);
            }

            if (isMaster != newMaster) {
                // 发生master/slave切换
                isMaster = newMaster;

                if (newMaster) {
                    log.info("node={}, 变成master", node);
                    // 切换到master
                    List<EnvVO> envVOList = envService.getAllEnvironments();
                    for (EnvVO envVO : envVOList) {
                        if (!envVO.getEnableHa()) {
                            continue;
                        }

                        List<ZoneDto> zoneDtoList = zoneService.fetchZonesByEnv(envVO.getName());
                        addPodHaController(envVO.getName(), zoneDtoList);
                    }

                } else {
                    // 切换到slave
                    log.info("node={}, 变成slave", node);
                    removeAllPodHaControllers();
                }
            } else if (isMaster) {
                List<EnvVO> envVOList = envService.getAllEnvironments();
                for (EnvVO envVO : envVOList) {
                    if (envVO.getEnableHa()) {
                        List<ZoneDto> zoneDtoList = zoneService.fetchZonesByEnv(envVO.getName());
                        if (!podHaControllerMap.containsKey(envVO.getName())) {
                            addPodHaController(envVO.getName(), zoneDtoList);

                        }

                    } else {
                        if (podHaControllerMap.containsKey(envVO.getName())) {
                            removePodHaController(envVO.getName());
                        }
                    }
                }

                for (Map.Entry<String, List<PodHaController>> entry : podHaControllerMap.entrySet())
                {
                    for (PodHaController podHaController : entry.getValue()) {
                        if (podHaController.isWatchClosed()) {
                            log.info("endpoints事件监听器已关闭, 重新开启, env={}, zone={}, apiserver={}",
                                    podHaController.getZone().getEnvName(),
                                    podHaController.getZone().getName(),
                                    podHaController.getZone().getK8s());

                            podHaController.startEventTrigger();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("高可用检查master/slave状态发生异常, err=" + ex.getMessage(), ex);
        }
    }
}
