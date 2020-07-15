package com.ppdai.stargate.controller;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.po.AuditLogEntity;
import com.ppdai.stargate.service.AuditService;
import com.ppdai.stargate.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditlogs")
public class AuditController {
    @Autowired
    private AuditService auditService;

    @RequestMapping(method = RequestMethod.GET)
    public Response<PageVO<AuditLogEntity>> getAuditLogsByPage(@RequestParam Integer page,
                                                               @RequestParam Integer size) {
        PageVO<AuditLogEntity> auditLogPageVO = auditService.fetchAuditLogsByPage(page, size);
        return Response.mark(MessageType.SUCCESS, auditLogPageVO);
    }
}
