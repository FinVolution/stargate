package com.ppdai.stargate.controller;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.remote.RemoteCmdb;
import com.ppdai.stargate.vo.OrgVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orgs")
@Slf4j
public class OrgController {
    @Autowired
    private RemoteCmdb remoteCmdb;

    @RequestMapping(method = RequestMethod.GET)
    public Response<List<OrgVO>> fetchOrgList() {
        List<OrgVO> orgVOs = remoteCmdb.fetchOrganizations();
        return Response.mark(MessageType.SUCCESS, orgVOs);
    }
}
