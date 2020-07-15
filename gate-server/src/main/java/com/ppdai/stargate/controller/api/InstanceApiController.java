package com.ppdai.stargate.controller.api;

import com.ppdai.atlas.client.model.ZoneDto;
import com.ppdai.stargate.constant.ConstantValue;
import com.ppdai.stargate.dto.*;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.*;
import com.ppdai.stargate.service.cloud.CloudInstanceService;
import com.ppdai.stargate.utils.TokenValidator;
import com.ppdai.stargate.vo.InstanceV3VO;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodCondition;
import io.kubernetes.client.models.V1PodList;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Api(description = "提供实例相关的一些操作")
@RestController
@RequestMapping("/api/cloud/instance")
@Slf4j
public class InstanceApiController {

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private ContainerService containerService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private CloudInstanceService cloudInstanceService;

    @Autowired
    private TokenValidator tokenValidator;

    @ApiOperation(value = "根据实例名查询实例", notes = "根据实例名查询实例", response = GetInstanceResponse.class)
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    GetInstanceResponse get(@RequestBody GetInstanceRequest request) {
        try {
            return cloudInstanceService.get(request);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new GetInstanceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "根据IP查询实例", notes = "根据IP查询实例", response = QueryInstanceRequest.class)
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    QueryInstanceResponse query(@RequestBody QueryInstanceRequest request) {
        try {
            return cloudInstanceService.query(request);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new QueryInstanceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "根据环境查询所有实例", notes = "根据环境查询所有实例", response = GetAllInstanceResponse.class)
    @RequestMapping(value = "/getall", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    GetAllInstanceResponse getAll(@RequestBody GetAllInstanceRequest request) {
        try {
            if (StringUtils.isEmpty(request.getEnv())) {
                return new GetAllInstanceResponse(-1, "参数[env]为空");
            }

            List<InstanceV3VO> instanceV3VOList = new ArrayList<>();

            V1PodList v1PodList = new V1PodList();
            List<String> k8sList = new ArrayList<>();
            List<ZoneDto> zoneDtos = zoneService.fetchZonesByEnv(request.getEnv());
            for (ZoneDto zoneDto : zoneDtos) {
                if (k8sList.contains(zoneDto.getK8s())) {
                    continue;
                }
                k8sList.add(zoneDto.getK8s());

                V1PodList podList = containerService.getContainersByZone(request.getEnv(), zoneDto.getName());
                v1PodList.getItems().addAll(podList.getItems());
            }

            List<InstanceEntity> instances = instanceService.findGroupInstancesByEnv(request.getEnv());
            for (int i = 0; i < instances.size(); i++) {
                InstanceEntity instance = instances.get(i);

                Optional<V1Pod> podOpt = v1PodList.getItems()
                        .stream()
                        .filter(x -> x.getMetadata().getName().equals(instance.getName()))
                        .findFirst();

                if (podOpt.isPresent()) {
                    V1Pod v1Pod = podOpt.get();
                    InstanceV3VO instanceV3VO = new InstanceV3VO();
                    instanceV3VO.setGroupId(instance.getGroupId());
                    instanceV3VO.setEnv(instance.getEnv());
                    instanceV3VO.setZone(instance.getZone());
                    instanceV3VO.setName(instance.getName());
                    instanceV3VO.setAppName(v1Pod.getMetadata().getLabels().get("app"));
                    instanceV3VO.setInstanceIp(v1Pod.getStatus().getPodIP());
                    instanceV3VO.setHostIp(v1Pod.getStatus().getHostIP());
                    instanceV3VO.setContainerStatus(v1Pod.getStatus().getPhase());
                    instanceV3VO.setStartTime(instance.getReleaseTime());
                    instanceV3VO.setImage(instance.getImage());
                    instanceV3VO.setSpec(instance.getSpec());
                    instanceV3VO.setPort(instance.getPort());

                    Map<String, Quantity> limits = v1Pod.getSpec().getContainers().get(0).getResources().getLimits();
                    instanceV3VO.setCpu(limits.get("cpu").toSuffixedString());
                    instanceV3VO.setMemory(limits.get("memory").toSuffixedString());

                    Boolean ready = false;
                    Optional<V1PodCondition> v1PodCondition = v1Pod.getStatus().getConditions()
                            .stream()
                            .filter(x -> x.getType().equals("Ready"))
                            .findFirst();
                    if (v1PodCondition.isPresent() && v1PodCondition.get().getStatus().equals("True")) {
                        ready = true;
                    }
                    instanceV3VO.setReady(ready);

                    instanceV3VOList.add(instanceV3VO);
                }
            }

            GetAllInstanceResponse response = new GetAllInstanceResponse(0, "success");
            response.setInstances(instanceV3VOList);

            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new GetAllInstanceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "部署实例", response = DeployInstanceResponse.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = ConstantValue.PAUTH_TOKEN_HEADER, value = "pauth的token", paramType = "header")
    })
    @RequestMapping(value = "/deploy", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    DeployInstanceResponse deploy(@RequestBody DeployInstanceRequest request, HttpServletRequest httpServletRequest) {
        try {
            tokenValidator.checkJwtToken(httpServletRequest);
            return cloudInstanceService.deploy(request);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new DeployInstanceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "更新实例", response = UpdateInstanceResponse.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = ConstantValue.PAUTH_TOKEN_HEADER, value = "pauth的token", paramType = "header")
    })
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    UpdateInstanceResponse update(@RequestBody UpdateInstanceRequest request, HttpServletRequest httpServletRequest) {
        try {
            tokenValidator.checkJwtToken(httpServletRequest);
            return cloudInstanceService.update(request);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new UpdateInstanceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "销毁实例", response = DestroyInstanceResponse.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = ConstantValue.PAUTH_TOKEN_HEADER, value = "pauth的token", paramType = "header")
    })
    @RequestMapping(value = "/destroy", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    DestroyInstanceResponse destroy(@RequestBody DestroyInstanceRequest request, HttpServletRequest httpServletRequest) {
        try {
            tokenValidator.checkJwtToken(httpServletRequest);
            return cloudInstanceService.destroy(request);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new DestroyInstanceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "重启实例", response = RestartInstanceResponse.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = ConstantValue.PAUTH_TOKEN_HEADER, value = "pauth的token", paramType = "header")
    })
    @RequestMapping(value = "/restart", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    RestartInstanceResponse restart(@RequestBody RestartInstanceRequest request, HttpServletRequest httpServletRequest) {
        try {
            tokenValidator.checkJwtToken(httpServletRequest);
            return cloudInstanceService.restart(request);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new RestartInstanceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "上线实例", response = UpInstanceResponse.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = ConstantValue.PAUTH_TOKEN_HEADER, value = "pauth的token", paramType = "header")
    })
    @RequestMapping(value = "/up", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    UpInstanceResponse up(@RequestBody UpInstanceRequest request, HttpServletRequest httpServletRequest) {
        try {
            tokenValidator.checkJwtToken(httpServletRequest);
            return cloudInstanceService.up(request);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new UpInstanceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "下线实例", response = DownInstanceResponse.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = ConstantValue.PAUTH_TOKEN_HEADER, value = "pauth的token", paramType = "header")
    })
    @RequestMapping(value = "/down", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    DownInstanceResponse down(@RequestBody DownInstanceRequest request, HttpServletRequest httpServletRequest) {
        try {
            tokenValidator.checkJwtToken(httpServletRequest);
            return cloudInstanceService.down(request);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new DownInstanceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "获取实例日志", response = GetInstanceLogResponse.class)
    @RequestMapping(value = "/log", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    GetInstanceLogResponse log(@RequestBody GetInstanceLogRequest request) {
        try {
            return cloudInstanceService.log(request);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new GetInstanceLogResponse(-1, ex.getMessage());
        }
    }
}
