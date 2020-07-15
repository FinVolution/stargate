package com.ppdai.stargate.controller.api;

import com.ppdai.stargate.dto.*;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.service.*;
import com.ppdai.stargate.vo.AppQuotaStatusVO;
import com.ppdai.stargate.vo.ImageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.*;

@Api(description = "提供应用相关的一些操作")
@RestController
@RequestMapping("/api/cloud/app")
@Slf4j
public class AppApiController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private AppService appService;

    @Autowired
    private ZoneService zoneService;

    @ApiOperation(value = "获取镜像列表", response = GetEnvImagesResponse.class)
    @RequestMapping(value = "/env/images", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    GetEnvImagesResponse images(@RequestBody GetEnvImagesRequest request) {

        try {
            if (StringUtils.isEmpty(request.getAppName())) {
                return new GetEnvImagesResponse(-1, "参数[appName]为空");
            }
            if (StringUtils.isEmpty(request.getEnv())) {
                return new GetEnvImagesResponse(-1, "参数[env]为空");
            }

            Map<String, Date> recentUsedImages = new HashMap<>();

            ApplicationEntity applicationEntity = appService.getAppByName(request.getAppName());

            if (applicationEntity == null) {
                return new GetEnvImagesResponse(-1, "无法找到应用, appName=" + request.getAppName());
            }

            List<Object[]> instanceEntities = instanceService.findRecentUsedImages(applicationEntity.getCmdbAppId(), request.getEnv());

            for (Object[] instanceEntity : instanceEntities) {
                if (!recentUsedImages.containsKey(instanceEntity[0].toString())) {
                    Timestamp timestamp = (Timestamp) instanceEntity[1];
                    recentUsedImages.put(instanceEntity[0].toString(), new Date(timestamp.getTime()));
                }
            }

            List<ImageVO> images = imageService.getImagesByAppName(request.getAppName(), recentUsedImages);
            List<GetEnvImagesResponse.ImageDTO> imageDTOList = new ArrayList<>();

            for (ImageVO imageVO : images) {
                GetEnvImagesResponse.ImageDTO imageDTO = new GetEnvImagesResponse.ImageDTO();
                imageDTO.setImage(imageVO.getName());
                imageDTO.setDeployTime(imageVO.getDeployAt());
                imageDTOList.add(imageDTO);
            }

            GetEnvImagesResponse response = new GetEnvImagesResponse(0, "success");
            response.setImages(imageDTOList);

            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new GetEnvImagesResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "获取镜像列表", response = GetImagesResponse.class)
    @RequestMapping(value = "/images", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    GetImagesResponse images(@RequestBody GetImagesRequest request) {

        try {
            if (StringUtils.isEmpty(request.getAppName())) {
                return new GetImagesResponse(-1, "参数[appName]为空");
            }

            List<String> images = new ArrayList<>();

            List<ImageVO> imageVOList = imageService.getImagesByAppName(request.getAppName());

            for (ImageVO imageVO : imageVOList) {
                images.add(imageVO.getName());
            }

            GetImagesResponse response = new GetImagesResponse(0, "success");
            response.setImages(images);

            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new GetImagesResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "获取配额列表", response = GetQuotaResponse.class)
    @RequestMapping(value = "/quotas", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    GetQuotaResponse quotas(@RequestBody GetQuotaRequest request) {
        try {
            if (StringUtils.isEmpty(request.getAppName())) {
                return new GetQuotaResponse(-1, "参数[appName]为空");
            }
            if (StringUtils.isEmpty(request.getEnv())) {
                return new GetQuotaResponse(-1, "参数[env]为空");
            }

            ApplicationEntity applicationEntity = appService.getAppByName(request.getAppName());

            if (applicationEntity == null) {
                return new GetQuotaResponse(-1, "无法找到应用, appName=" + request.getAppName());
            }

            List<AppQuotaStatusVO> appQuotaStatusVOS = appService.fetchAppQuotaStatusByAppAndEnv(applicationEntity.getCmdbAppId(), request.getEnv());

            List<GetQuotaResponse.QuotaDTO> quotaDTOList = new ArrayList<>();

            for (AppQuotaStatusVO appQuotaStatusVO : appQuotaStatusVOS) {
                GetQuotaResponse.QuotaDTO quotaDTO = new GetQuotaResponse.QuotaDTO();
                quotaDTO.setSpec(appQuotaStatusVO.getSpectypeName());
                quotaDTO.setTotal(appQuotaStatusVO.getTotal());
                quotaDTO.setFree(appQuotaStatusVO.getFreeCount());
                quotaDTO.setUsed(appQuotaStatusVO.getUsedCount());
                quotaDTOList.add(quotaDTO);
            }

            GetQuotaResponse response = new GetQuotaResponse(0, "success");
            response.setQuotas(quotaDTOList);

            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new GetQuotaResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "获取zone列表", response = GetZonesResponse.class)
    @RequestMapping(value = "/zones", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    GetZonesResponse zones(@RequestBody GetZonesRequest request) {
        try {
            if (StringUtils.isEmpty(request.getAppName())) {
                return new GetZonesResponse(-1, "参数[appName]为空");
            }
            if (StringUtils.isEmpty(request.getEnv())) {
                return new GetZonesResponse(-1, "参数[env]为空");
            }

            List<String> zones = zoneService.fetchZoneNamesByEnv(request.getEnv());

            GetZonesResponse response = new GetZonesResponse(0, "success");
            response.setZones(zones);

            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new GetZonesResponse(-1, ex.getMessage());
        }
    }
}
