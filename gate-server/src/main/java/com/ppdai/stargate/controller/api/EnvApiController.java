package com.ppdai.stargate.controller.api;

import com.ppdai.stargate.dto.ListEnvRequest;
import com.ppdai.stargate.dto.ListEnvResponse;
import com.ppdai.stargate.service.EnvService;
import com.ppdai.stargate.vo.EnvVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Api(description = "提供环境相关的一些操作")
@RestController
@RequestMapping("/api/cloud/env")
@Slf4j
public class EnvApiController {

    @Autowired
    private EnvService envService;

    @ApiOperation(value = "获取环境列表", response = ListEnvResponse.class)
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    ListEnvResponse list() {
        try {
            List<EnvVO> envList = envService.getInUseEnvironments();
            List<String> envs = new ArrayList<>();

            for (EnvVO envVO : envList) {
                envs.add(envVO.getName());
            }

            ListEnvResponse response = new ListEnvResponse(0, "success");
            response.setEnvs(envs);

            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ListEnvResponse(-1, ex.getMessage());
        }
    }
}
