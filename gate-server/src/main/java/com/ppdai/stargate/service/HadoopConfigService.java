package com.ppdai.stargate.service;


import com.ppdai.stargate.client.K8sClient;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.dao.HadoopConfigRepository;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.po.HadoopConfigEntity;
import com.ppdai.stargate.remote.RemoteManager;
import io.kubernetes.client.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class HadoopConfigService {
    @Autowired
    private HadoopConfigRepository hadoopConfigRepository;
    @Autowired
    private RemoteManager remoteManager;
    @Autowired
    private ZoneService zoneService;


    public List<HadoopConfigEntity> listHadoopConfigs(String env) {
        return hadoopConfigRepository.findAllByEnvAndIsActiveTrue(env);
    }

    public synchronized void createConfig(HadoopConfigEntity hadoopConfigEntity) throws ApiException {
        String hadoopName = hadoopConfigEntity.getName();
        String env = hadoopConfigEntity.getEnv();
        HadoopConfigEntity hadoopConfigEntityFromDb = hadoopConfigRepository.findByEnvAndNameAndIsActiveTrue(env,hadoopName);
        if (hadoopConfigEntityFromDb != null) {
            throw BaseException.newException(MessageType.ERROR, "创建hadoop配置失败，名称已经存在：" + hadoopName);
        }
        List<String> zones = zoneService.fetchZoneNamesByEnv(env);
        log.info("创建hadoop配置，zones={},env={}", zones, env);
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("core-site.xml", hadoopConfigEntity.getCoreSite());
        dataMap.put("hdfs-site.xml", hadoopConfigEntity.getHdfsSite());
        for (String zone : zones) {
            K8sClient k8sClientByZone = remoteManager.getK8sClientByZone(env, zone);
            //fixme 该参数应该从页面传递过来，临时应急
            String namespace = "bigdata";
            k8sClientByZone.createConfigMap(namespace, hadoopName, dataMap);
        }
        hadoopConfigRepository.saveAndFlush(hadoopConfigEntity);
    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteHadoopConfig(String env, Long id) throws ApiException {
        List<String> zones = zoneService.fetchZoneNamesByEnv(env);
        log.info("删除hadoop配置，zones={},env={}", zones, env);
        HadoopConfigEntity hadoopConfigEntityFromDb = hadoopConfigRepository.findOne(id);
        hadoopConfigRepository.deleteById(id);
        for (String zone : zones) {
            K8sClient k8sClientByZone = remoteManager.getK8sClientByZone(env, zone);
            String namespace = "bigdata";
            k8sClientByZone.deleteConfigMap( namespace, hadoopConfigEntityFromDb.getName());
        }
    }

    public HadoopConfigEntity findHadoopConfigByName(String env, String hadoopConfig) {
        return hadoopConfigRepository.findByEnvAndNameAndIsActiveTrue(env, hadoopConfig);
    }

    public void updateConfig(HadoopConfigEntity hadoopConfigEntity) {
        hadoopConfigRepository.saveAndFlush(hadoopConfigEntity);
    }

    public HadoopConfigEntity findHadoopConfigById(Long id) {
        return hadoopConfigRepository.findOne(id);
    }

    public List<HadoopConfigEntity> listHadoopConfigs(String env, String department) {
        return listHadoopConfigs(env).stream()
                .filter(hadoopConfigEntity -> department.equalsIgnoreCase(hadoopConfigEntity.getDepartment()))
                .collect(toList());
    }
}
