package com.ppdai.stargate.service;

import com.google.common.base.Preconditions;
import com.ppdai.stargate.dao.AuditLogRepository;
import com.ppdai.stargate.po.AuditLogEntity;
import com.ppdai.stargate.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuditService {

    @Autowired
    AuditLogRepository auditLogRepo;

    @Transactional
    public void recordOperation(AuditLogEntity actionItem) {
        Preconditions.checkNotNull(actionItem, "action can not be null");
        auditLogRepo.save(actionItem);
    }

    public PageVO<AuditLogEntity> fetchAuditLogsByPage(int page, int size) {
        PageVO<AuditLogEntity> pageVO = new PageVO<>();

        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(page - 1, size, sort);
        Page<AuditLogEntity> auditLogPage = auditLogRepo.findAll(pageable);

        pageVO.setContent(auditLogPage.getContent());
        pageVO.setTotalElements(auditLogPage.getTotalElements());
        return pageVO;
    }

}
