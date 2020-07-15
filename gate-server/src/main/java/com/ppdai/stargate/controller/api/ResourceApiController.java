package com.ppdai.stargate.controller.api;

import com.ppdai.stargate.dto.*;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.ResourceEntity;
import com.ppdai.stargate.service.AppService;
import com.ppdai.stargate.service.ResourceService;
import com.ppdai.stargate.service.ZoneService;
import com.ppdai.stargate.utils.TokenValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(description = "提供资源相关的一些操作")
@RestController
@RequestMapping("/api/cloud/resource")
@Slf4j

public class ResourceApiController {
    @Autowired
    private ResourceService resourceService;

    @Autowired
    private AppService appService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private TokenValidator tokenValidator;

    @ApiOperation(value = "查询所有资源", notes = "查询所有资源", response = GetResourceResponse.class)
    @RequestMapping(value = "/get/all", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    GetResourceResponse getAllResources(@RequestBody GetResourceRequest request) {
        try {
            if (request.getAppId() == null) {
                return new GetResourceResponse(-1, "参数[appId]为空");
            }

            if (request.getEnv() == null) {
                return new GetResourceResponse(-1, "参数[env]为空");
            }

            List<ResourceEntity> resourceEntityList = resourceService.findByAppIdAndEnv(request.getAppId(), request.getEnv());

            GetResourceResponse response = new GetResourceResponse(0, "success");
            response.setResources(resourceEntityList);
            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new GetResourceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "查询可用静态资源", notes = "查询可用静态资源", response = GetResourceResponse.class)
    @RequestMapping(value = "/get/available", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    GetResourceResponse getAvailableStaticResources(@RequestBody GetResourceRequest request) {
        try {
            if (request.getAppId() == null) {
                return new GetResourceResponse(-1, "参数[appId]为空");
            }

            if (request.getEnv() == null) {
                return new GetResourceResponse(-1, "参数[env]为空");
            }

            if (request.getSpec() == null) {
                return new GetResourceResponse(-1, "参数[spec]为空");
            }

            List<ResourceEntity> resourceEntityList = resourceService.findAvailableStaticResources(request.getAppId(), request.getEnv(), request.getSpec());

            GetResourceResponse response = new GetResourceResponse(0, "success");
            response.setResources(resourceEntityList);
            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new GetResourceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "根据实例创建资源（多环境）", notes = "根据实例创建资源（多环境）", response = CreateResourceResponse.class)
    @RequestMapping(value = "/create/one", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    CreateResourceResponse createResource(@RequestBody CreateResourceRequest request, HttpServletRequest httpServletRequest) {
        try {
            tokenValidator.checkJwtToken(httpServletRequest);

            if (request.getAppId() == null) {
                return new CreateResourceResponse(-1, "参数[appId]为空");
            }

            if (request.getEnv() == null) {
                return new CreateResourceResponse(-1, "参数[env]为空");
            }

            if (request.getSpec() == null) {
                return new CreateResourceResponse(-1, "参数[spec]为空");
            }

            if (request.getPodName() == null) {
                return new CreateResourceResponse(-1, "参数[podName]为空");
            }

            if (request.getIsStatic() == null) {
                return new CreateResourceResponse(-1, "参数[isStatic]为空");
            }

            ApplicationEntity applicationEntity = appService.getAppByCmdbId(request.getAppId());
            List<String> zones = zoneService.fetchZoneNamesByEnv(request.getEnv());
            ResourceEntity resourceEntity = resourceService.createResourceByInstance(request.getAppId(), applicationEntity.getName(), request.getEnv(), request.getSpec(), request.getPodName(), zones.get(0), request.getIsStatic());

            CreateResourceResponse response = new CreateResourceResponse(0, "success");
            response.setResource(resourceEntity);
            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new CreateResourceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "根据实例释放资源（多环境）", notes = "根据实例释放资源（多环境）", response = ReleaseResourceResponse.class)
    @RequestMapping(value = "/release/one", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    ReleaseResourceResponse createResource(@RequestBody ReleaseResourceRequest request, HttpServletRequest httpServletRequest) {
        try {
            tokenValidator.checkJwtToken(httpServletRequest);

            if (request.getPodName() == null) {
                return new ReleaseResourceResponse(-1, "参数[podName]为空");
            }

            resourceService.releaseResourceByInstance(request.getPodName());

            ReleaseResourceResponse response = new ReleaseResourceResponse(0, "success");
            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ReleaseResourceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "添加静态资源", notes = "添加静态资源", response = AddStaticResourceResponse.class)
    @RequestMapping(value = "/add/static", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    AddStaticResourceResponse addStaticResource(@RequestBody AddStaticResourceRequest request, HttpServletRequest httpServletRequest) {
        try {
            tokenValidator.checkJwtToken(httpServletRequest);

            if (request.getAppId() == null) {
                return new AddStaticResourceResponse(-1, "参数[appId]为空");
            }

            if (request.getEnv() == null) {
                return new AddStaticResourceResponse(-1, "参数[env]为空");
            }

            if (request.getSpec() == null) {
                return new AddStaticResourceResponse(-1, "参数[spec]为空");
            }

            if (request.getNumber() == null) {
                return new AddStaticResourceResponse(-1, "参数[number]为空");
            }

            resourceService.addStaticResource(request.getAppId(), request.getEnv(), request.getSpec(), request.getNumber(), request.getZone());

            return new AddStaticResourceResponse(0, "success");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new AddStaticResourceResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "转换资源类型", notes = "转换资源类型", response = ChangeResourceTypeResponse.class)
    @RequestMapping(value = "/changeType", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    ChangeResourceTypeResponse changeResourceType(@RequestBody ChangeResourceTypeRequest request, HttpServletRequest httpServletRequest) {
        try {
            tokenValidator.checkJwtToken(httpServletRequest);

            if (request.getAppId() == null) {
                return new ChangeResourceTypeResponse(-1, "参数[appId]为空");
            }

            if (request.getEnv() == null) {
                return new ChangeResourceTypeResponse(-1, "参数[env]为空");
            }

            if (request.getIsStatic() == null) {
                return new ChangeResourceTypeResponse(-1, "参数[isStatic]为空");
            }

            int count = resourceService.changeTypeForAllResources(request.getAppId(), request.getEnv(), request.getIsStatic());

            ChangeResourceTypeResponse response = new ChangeResourceTypeResponse(0, "success");
            response.setCount(count);
            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ChangeResourceTypeResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "删除资源", notes = "删除资源", response = DeleteResourceResponse.class)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    DeleteResourceResponse deleteResource(@RequestBody DeleteResourceRequest request, HttpServletRequest httpServletRequest) {
        try {
            tokenValidator.checkJwtToken(httpServletRequest);

            if (request.getResourceId() == null) {
                return new DeleteResourceResponse(-1, "参数[resourceId]为空");
            }

            resourceService.removeResourceById(request.getResourceId());

            return new DeleteResourceResponse(0, "success");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new DeleteResourceResponse(-1, ex.getMessage());
        }
    }
}
