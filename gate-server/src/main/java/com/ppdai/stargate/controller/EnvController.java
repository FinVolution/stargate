package com.ppdai.stargate.controller;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.service.EnvService;
import com.ppdai.stargate.service.GroupService;
import com.ppdai.stargate.vo.EnvVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/envs")
@Slf4j
public class EnvController {

    @Autowired
    private EnvService envService;

    @RequestMapping(method = RequestMethod.GET)
    public Response<List<EnvVO>> findAllEnvs() {
        List<EnvVO> envList = envService.getAllEnvironments();
        return Response.mark(MessageType.SUCCESS, envList);
    }

    @RequestMapping(value = "/activate", method = RequestMethod.GET)
    public Response<List<EnvVO>> findActivateEnvs() {
        List<EnvVO> envList = envService.getInUseEnvironments();
        return Response.mark(MessageType.SUCCESS, envList);
    }

    @RequestMapping(value = "/activate", method = RequestMethod.POST)
    public Response<String> postActivateEnvironment(@RequestParam(value = "envId") Long envId,
                                                    @RequestParam(value = "activated") Boolean isInUse) {
        Boolean bSuccess = envService.setEnvironmentInUse(envId, isInUse);
        String msg = bSuccess ? "环境管理状态设置成功。" : "环境管理状态设置失败。";
        MessageType msgType = bSuccess ? MessageType.SUCCESS : MessageType.ERROR;
        return Response.mark(msgType, msg);
    }

    @RequestMapping(value = "/enableHa", method = RequestMethod.POST)
    public Response<String>  enableHa(@RequestParam(value = "envId") Long envId) {
        Boolean bSuccess = envService.setEnvironmentEnableHa(envId, true);
        String msg = bSuccess ? "启用高可用成功。" : "启用高可用失败。";
        MessageType msgType = bSuccess ? MessageType.SUCCESS : MessageType.ERROR;
        return Response.mark(msgType, msg);
    }

    @RequestMapping(value = "/disableHa", method = RequestMethod.POST)
    public Response<String>  disableHa(@RequestParam(value = "envId") Long envId) {
        Boolean bSuccess = envService.setEnvironmentEnableHa(envId, false);
        String msg = bSuccess ? "禁用高可用成功。" : "禁用高可用失败。";
        MessageType msgType = bSuccess ? MessageType.SUCCESS : MessageType.ERROR;
        return Response.mark(msgType, msg);
    }

}
