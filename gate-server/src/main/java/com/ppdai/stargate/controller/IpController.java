package com.ppdai.stargate.controller;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.vi.AddIpRequestVI;
import com.ppdai.stargate.po.IpEntity;
import com.ppdai.stargate.service.IpService;
import com.ppdai.stargate.vo.PageVO;
import io.kubernetes.client.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ips")
@Slf4j
public class IpController {

    @Autowired
    private IpService ipService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Response<String> addNewIp(@RequestBody AddIpRequestVI addIpRequestVI) {
        int count = ipService.AddIp(addIpRequestVI);
        return Response.mark(MessageType.SUCCESS, "ip添加成功, 数量 = %d", count);
    }

    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Response<PageVO<IpEntity>> findIps(@RequestParam(required = false) String network,
                                              @RequestParam(required = false) String networkSegment,
                                              @RequestParam(required = false) String ip,
                                              @RequestParam(required = false) Boolean occupied,
                                              @RequestParam Integer page,
                                              @RequestParam Integer size) {
        PageVO<IpEntity> ipPageVO = ipService.findIpsByPage(network, networkSegment, ip, occupied, page, size);
        return Response.success(ipPageVO);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Response<String> removeIp(@PathVariable("id") Long id) {
        ipService.removeIpById(id);
        return Response.success("ip删除成功");
    }

    @RequestMapping(value = "/syncstatus", method = RequestMethod.POST)
    public Response<String> syncIpStatus(@RequestParam String env) throws ApiException {
        ipService.syncIpStatusByEnv(env);
        return Response.mark(MessageType.SUCCESS, "同步ip状态成功, env = %s", env);
    }

    @RequestMapping(value = "/syncstatus/db", method = RequestMethod.POST)
    public Response<String> syncIpStatusFromDB() {
        ipService.syncIpStatusFromDB();
        return Response.success("同步ip状态成功");
    }
}