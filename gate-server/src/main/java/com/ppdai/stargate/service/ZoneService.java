package com.ppdai.stargate.service;

import com.ppdai.atlas.client.model.ZoneDto;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.remote.RemoteCmdb;
import com.ppdai.stargate.vo.ZoneInstanceCountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ZoneService {
    @Autowired
    private RemoteCmdb remoteCmdb;

    @Autowired
    private InstanceService instanceService;

    public List<ZoneDto> fetchZonesByEnv(String env) {
        return remoteCmdb.fetchZonesByEnv(env);
    }

    public List<ZoneDto> fetchAllZones() {
        return remoteCmdb.fetchAllZones();
    }

    public List<String> fetchZoneNamesByEnv(String env) {
        List<String> zones = new ArrayList<>();
        List<ZoneDto> zoneDtos = remoteCmdb.fetchZonesByEnv(env);
        for (int i = 0; i < zoneDtos.size(); i++) {
            ZoneDto zoneDto = zoneDtos.get(i);
            zones.add(zoneDto.getName());
        }
        return zones;
    }

    public String fetchDashboardByZoneAndEnv(String zone, String env) {
        String dashboard = null;
        List<ZoneDto> zoneDtos = remoteCmdb.fetchZonesByEnv(env);
        for (int i = 0; i < zoneDtos.size(); i++) {
            ZoneDto zoneDto = zoneDtos.get(i);
            if (zoneDto.getName().equals(zone)) {
                Map extensions = (Map) zoneDto.getExtensions();
                Object value = extensions.get("dashboard");
                if (value != null) {
                    dashboard = value.toString();
                }
                break;
            }
        }
        return dashboard;
    }

    public String fetchNetworkByZoneAndEnv(String zone, String env) {
        String network = null;
        List<ZoneDto> zoneDtos = remoteCmdb.fetchZonesByEnv(env);
        for (int i = 0; i < zoneDtos.size(); i++) {
            ZoneDto zoneDto = zoneDtos.get(i);
            if (zoneDto.getName().equals(zone)) {
                Map extensions = (Map) zoneDto.getExtensions();
                Object value = extensions.get("network");
                if (value != null) {
                    network = value.toString();
                }
                break;
            }
        }
        return network;
    }

    /**
     * 根据要发布的实例数获取分配好的zone列表
     * @param env
     * @param zone
     * @param instanceCount
     * @param groupId
     * @return
     */
    public List<String> getZoneListByInstanceCount(String env, String zone, int instanceCount, Long groupId) {
        List<String> zoneList = new ArrayList<>();

        String[] zones = zone.split(",");

        if (zones.length == 1) {
            for (int i = 0; i < instanceCount; i++) {
                zoneList.add(zone);
            }
            return zoneList;
        }

        List<ZoneInstanceCountVO> zoneInstanceCountVOList = new ArrayList<>();
        for (int i = 0; i < zones.length; i++) {
            ZoneInstanceCountVO zoneInstanceCountVO = new ZoneInstanceCountVO();
            zoneInstanceCountVO.setZone(zones[i]);
            Integer usedCount = instanceService.findInstanceCountByGroupIdAndZone(groupId, zones[i]);
            zoneInstanceCountVO.setUsedCount(usedCount);
            zoneInstanceCountVOList.add(zoneInstanceCountVO);
        }

        if (zoneInstanceCountVOList.isEmpty()) {
            throw BaseException.newException(MessageType.ERROR, "没有可调度的集群, env = %s, zones = %s", env, zone);
        }

        // zone按已有实例数升序排列
        zoneInstanceCountVOList.sort(Comparator.comparingInt(ZoneInstanceCountVO::getUsedCount));

        // 遍历zone，一次分配一个实例，直到分完
        while (instanceCount > 0) {
            for (ZoneInstanceCountVO zoneInstanceCountVO : zoneInstanceCountVOList) {
                zoneList.add(zoneInstanceCountVO.getZone());
                instanceCount--;
                if (instanceCount <= 0) {
                    break;
                }
            }
        }

        return zoneList;
    }
}
