package com.ppdai.stargate.service;

import com.ppdai.stargate.dao.DnsRepository;
import com.ppdai.stargate.dto.DnsDto;
import com.ppdai.stargate.po.DnsEntity;
import com.ppdai.stargate.utils.ConvertUtil;
import com.ppdai.stargate.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DnsService {

    @Autowired
    private DnsRepository dnsRepository;

    public DnsEntity getById(Long id) {
        return dnsRepository.getOne(id);
    }

    public void add(List<DnsDto> recordDtoList) {

        for (DnsDto recordDto : recordDtoList) {

            DnsEntity recordEntity = ConvertUtil.convert(recordDto, DnsEntity.class);

            DnsEntity recordEntityFromDb = dnsRepository.findByNameAndEnvId(recordEntity.getName(), recordEntity.getEnvId());
            if (recordEntityFromDb != null) {
                recordEntity.setId(recordEntityFromDb.getId());
            }

            dnsRepository.save(recordEntity);
        }

    }

    public DnsDto update(DnsEntity recordEntity) {
        return ConvertUtil.convert(dnsRepository.save(recordEntity), DnsDto.class);
    }

    public void deleteByNameAndEnvId(String name, String envId) {
        dnsRepository.deleteByNameAndEnvId(name, envId);
    }

    public void deleteById(Long id) {
        try {
            log.info("delete dns: " + id);
            dnsRepository.delete(id);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public PageVO<DnsDto> getByPage(String name, String envId, String content, int page, int size) {
        PageVO<DnsDto> applyPageVO = new PageVO<>();
        Pageable pageable = new PageRequest(page - 1, size, Sort.Direction.DESC, "updateTime");

        Page<DnsEntity> applyPage = dnsRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();

            if (name != null && !name.trim().equals("")) {
                list.add(criteriaBuilder.like(root.get("name").as(String.class), "%" + name.trim() + "%"));
            }

            if (envId != null && !envId.trim().equals("")) {
                list.add(criteriaBuilder.equal(root.get("envId").as(String.class), envId.trim()));
            }

            if (content != null && !content.trim().equals("")) {
                list.add(criteriaBuilder.equal(root.get("content").as(String.class), content.trim()));
            }

            list.add(criteriaBuilder.equal(root.get("isActive").as(Boolean.class), Boolean.TRUE));

            Predicate[] p = new Predicate[list.size()];
            return criteriaBuilder.and(list.toArray(p));
        }, pageable);


        List<DnsEntity> applyEntitiesList = applyPage.getContent();
        List<DnsDto> applyList = ConvertUtil.convert(applyEntitiesList, DnsDto.class);

        applyPageVO.setContent(applyList);
        applyPageVO.setTotalElements(applyPage.getTotalElements());
        return applyPageVO;
    }

    public DnsDto findRecordByNameAndEnvId(String name, String envId) {
        DnsEntity entity = dnsRepository.findByNameAndEnvId(name, envId);
        if (entity == null) {
            return null;
        }
        return ConvertUtil.convert(entity, DnsDto.class);
    }

    public List<DnsDto> findRecordByEnvId(String envId) {
        List<DnsEntity> recordEntities = dnsRepository.findByEnvId(envId);
        return ConvertUtil.convert(recordEntities, DnsDto.class);
    }
}
