package com.ppdai.stargate.controller;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.dto.*;
import com.ppdai.stargate.po.DnsEntity;
import com.ppdai.stargate.service.DnsService;
import com.ppdai.stargate.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/dns")
@Slf4j
public class DnsController {

    @Autowired
    private DnsService dnsService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Response<String> add(@RequestBody CreateDnsRequest request) {

        dnsService.add(request.getRecords());

        return Response.mark(MessageType.SUCCESS, "ok");
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Response<String> update(@RequestBody UpdateDnsRequest request) {

        DnsDto recordEntityFromDb = dnsService.findRecordByNameAndEnvId(request.getName(), request.getEnvId());

        //判断是否已经存在其他的name-envId记录
        if (recordEntityFromDb != null && recordEntityFromDb.getId().longValue() != request.getId().longValue()) {
            return Response.mark(MessageType.ERROR, "指定记录 name=" + request.getName() + ", envid=" + request.getEnvId() + " 已存在");
        }

        DnsEntity recordEntity = dnsService.getById(request.getId());
        if (recordEntity == null) {
            return Response.mark(MessageType.ERROR, "fail");
        }

        if (!StringUtils.isEmpty(request.getName())) {
            recordEntity.setName(request.getName());
        }

        if (!StringUtils.isEmpty(request.getType())) {
            recordEntity.setType(request.getType());
        }

        if (!StringUtils.isEmpty(request.getContent())) {
            recordEntity.setContent(request.getContent());
        }

        if (!StringUtils.isEmpty(request.getEnvId())) {
            recordEntity.setEnvId(request.getEnvId());
        }

        dnsService.update(recordEntity);
        return Response.mark(MessageType.SUCCESS, "OK");
    }

    @RequestMapping(value = "/deleteByNameAndEnvId", method = RequestMethod.POST)
    public Response<String> deleteByNameAndEnvId(@RequestBody DeleteDnsByNameAndEnvIdRequest request) {
        dnsService.deleteByNameAndEnvId(request.getName(), request.getEnvId());

        return Response.mark(MessageType.SUCCESS, "ok");
    }

    @RequestMapping(value = "/deleteById", method = RequestMethod.POST)
    public Response<String> deleteById(@RequestBody DeleteDnsByIdRequest request) {
        dnsService.deleteById(request.getId());

        return Response.mark(MessageType.SUCCESS, "ok");
    }

    @RequestMapping(value = "/queryByPage", method = RequestMethod.GET)
    public Response<PageVO<DnsDto>> queryByPage(@RequestParam(value = "name", required = false) String name,
                                                @RequestParam(value = "envId", required = false) String envId,
                                                @RequestParam(value = "content", required = false) String content,
                                                @RequestParam(value = "page") Integer page,
                                                @RequestParam(value = "size") Integer size) {
        PageVO<DnsDto> sitePageVO = dnsService.getByPage(name, envId, content, page, size);
        return Response.mark(MessageType.SUCCESS, sitePageVO);
    }

    @RequestMapping(path = "/export", method = RequestMethod.GET)
    public ResponseEntity<Resource> export(@RequestParam(value = "envId", required = true) String env) throws IOException {
        List<DnsDto> recordDtoList = dnsService.findRecordByEnvId(env);
        StringBuilder sb = new StringBuilder();

        for (DnsDto recordDto : recordDtoList) {
            sb.append(recordDto.getContent()).append(" ").append(recordDto.getName()).append("\n");
        }

        String content = sb.toString();
        byte[] contentBytes = content.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"host\""));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(contentBytes.length)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new ByteArrayResource(contentBytes));
    }
}
