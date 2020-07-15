package com.ppdai.stargate.service;

import com.ppdai.atlas.client.model.ZoneDto;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.dao.InstanceRepository;
import com.ppdai.stargate.dao.IpRepository;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.validator.IpServiceValidator;
import com.ppdai.stargate.vi.AddIpRequestVI;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.po.IpEntity;
import com.ppdai.stargate.vo.PageVO;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;

@Slf4j
@Service
public class IpService {

    @Autowired
    private IpRepository ipRepo;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private ContainerService containerService;

    @Autowired
    private InstanceRepository instanceRepo;

    @Autowired
    private IpServiceValidator ipServiceValidator;

    private boolean isIp(String IP) {
        boolean b = false;

        if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String[] s = IP.split("\\.");
            if (Integer.parseInt(s[0]) < 255)
                if (Integer.parseInt(s[1]) < 255)
                    if (Integer.parseInt(s[2]) < 255)
                        if (Integer.parseInt(s[3]) < 255)
                            b = true;
        }
        return b;
    }

    private String getSubNet(String IP) {
        if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String[] s = IP.split("\\.");
            return s[0] + "." + s[1] + "." + s[2] + ".";
        } else {
            return "";
        }
    }

    public int AddIp(AddIpRequestVI addIpRequestVI) {
        ipServiceValidator.AddIpValidator(addIpRequestVI);

        List<IpEntity> ipEntityList = new ArrayList<>();

        for (int i = addIpRequestVI.getMinIp(); i <= addIpRequestVI.getMaxIp(); i++) {
            String ip = getSubNet(addIpRequestVI.getNetworkSegment()) + i;

            IpEntity oldIp = ipRepo.findByIpEx(ip);
            if (oldIp == null) {
                IpEntity ipEntity = new IpEntity();
                ipEntity.setNetwork(addIpRequestVI.getNetwork());
                ipEntity.setNetworkSegment(addIpRequestVI.getNetworkSegment());
                ipEntity.setIp(ip);
                ipEntity.setOccupied(false);
                ipEntityList.add(ipEntity);
            } else if (oldIp.getIsActive() == false) {
                oldIp.setNetwork(addIpRequestVI.getNetwork());
                oldIp.setNetworkSegment(addIpRequestVI.getNetworkSegment());
                oldIp.setOccupied(false);
                oldIp.setIsActive(true);
                ipEntityList.add(oldIp);
            } else {
                throw BaseException.newException(MessageType.ERROR, "IP[%s]已存在", ip);
            }
        }

        ipRepo.save(ipEntityList);
        ipRepo.flush();

        return ipEntityList.size();
    }

    public PageVO<IpEntity> findIpsByPage(String network, String networkSegment, String ip, Boolean occupied, Integer page, Integer size) {
        PageVO<IpEntity> ipPageVO = new PageVO<>();

        Pageable pageable = new PageRequest(page - 1, size);

        Page<IpEntity> ipPage = ipRepo.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();

            if (StringUtils.isNotEmpty(network)) {
                list.add(criteriaBuilder.equal(root.get("network").as(String.class), network));
            }

            if (StringUtils.isNotEmpty(networkSegment)) {
                list.add(criteriaBuilder.equal(root.get("networkSegment").as(String.class), networkSegment));
            }

            if (StringUtils.isNotEmpty(ip)) {
                list.add(criteriaBuilder.equal(root.get("ip").as(String.class), ip));
            }

            if (occupied != null) {
                list.add(criteriaBuilder.equal(root.get("occupied").as(Boolean.class), occupied));
            }

            list.add(criteriaBuilder.equal(root.get("isActive").as(Boolean.class), Boolean.TRUE));

            Predicate[] p = new Predicate[list.size()];
            return criteriaBuilder.and(list.toArray(p));
        }, pageable);

        ipPageVO.setContent(ipPage.getContent());
        ipPageVO.setTotalElements(ipPage.getTotalElements());

        return ipPageVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeIpById(Long id) {
        IpEntity ipEntity = ipRepo.findById(id);
        if (ipEntity == null) {
            throw BaseException.newException(MessageType.ERROR, "ip[id=%d]不存在", id);
        }
        if (ipEntity.getOccupied() == true) {
            throw BaseException.newException(MessageType.ERROR, "ip[%s]已被占用", ipEntity.getIp());
        }
        ipEntity.setIsActive(false);
        ipRepo.saveAndFlush(ipEntity);
    }

    public List<IpEntity> findUnoccupiedIp(String network) {
        return ipRepo.findUnoccupiedIp(network);
    }

    public void syncIpStatusByEnv(String env) throws ApiException {
        List<IpEntity> ipEntityList = new ArrayList<>();

        List<ZoneDto> zoneDtos = zoneService.fetchZonesByEnv(env);
        List<String> k8sList = new ArrayList<>();

        for (int i = 0; i < zoneDtos.size(); i++) {
            ZoneDto zoneDto = zoneDtos.get(i);

            if (k8sList.contains(zoneDto.getK8s())) {
                continue;
            }
            k8sList.add(zoneDto.getK8s());

            String zone = zoneDto.getName();

            V1PodList v1PodList = containerService.getContainersByZone(env, zone);

            for (V1Pod v1Pod : v1PodList.getItems()) {
                if (v1Pod.getStatus().getPodIP() != null) {
                    IpEntity ipEntity = ipRepo.findByIp(v1Pod.getStatus().getPodIP());
                    if (ipEntity != null) {
                        ipEntity.setOccupied(true);
                        ipEntityList.add(ipEntity);
                    }
                }
            }
        }

        ipRepo.save(ipEntityList);
        ipRepo.flush();
    }

    public void syncIpStatusFromDB() {
        List<IpEntity> ipEntityList = new ArrayList<>();

        List<InstanceEntity> instanceEntityList = instanceRepo.findAllInstances();
        for (int i = 0; i < instanceEntityList.size(); i++) {
            InstanceEntity instanceEntity = instanceEntityList.get(i);
            if (instanceEntity.getSlotIp() != null) {
                IpEntity ipEntity = ipRepo.findByIp(instanceEntity.getSlotIp());
                if (ipEntity != null) {
                    ipEntity.setOccupied(true);
                    ipEntityList.add(ipEntity);
                }
            }
        }

        ipRepo.save(ipEntityList);
        ipRepo.flush();
    }
}
