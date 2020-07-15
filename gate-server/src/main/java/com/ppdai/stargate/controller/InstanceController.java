package com.ppdai.stargate.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ppdai.stargate.client.JsonHttpClient;
import com.ppdai.stargate.dto.GetInstanceLogRequest;
import com.ppdai.stargate.dto.GetInstanceLogResponse;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.*;
import com.ppdai.stargate.service.cloud.CloudInstanceService;
import com.ppdai.stargate.vo.*;
import io.kubernetes.client.ApiException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/instances")
@Slf4j
public class InstanceController {

    @Autowired
    private CloudInstanceService cloudInstanceService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AppService appService;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private JsonHttpClient hcHttpClient;

    @Autowired
    private ExecCommandService execCommandService;
    /**
     * 获取指定站点发布组的服务器实例列表
     *
     * @param groupId 指定的发布组ID
     * @return 返回站点发布组内的服务器实例列表
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Response<List<InstanceVO>> findInstancesByGroupId(@RequestParam(value = "groupId") Long groupId) {
        List<InstanceVO> instances = instanceService.getInstanceStatusByGroupId(groupId);
        return Response.mark(MessageType.SUCCESS, instances);
    }

    /**
     * 获取数据库中所有存活的实例
     * @return
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public Response<List<InstanceEntity>> findAllInstances() {
        List<InstanceEntity> allInstances = instanceService.findAllInstances();
        return Response.success(allInstances);
    }

    /**
     * 获取k8s中的所有实例
     * @return
     */
    @RequestMapping(value = "/k8s", method = RequestMethod.GET)
    public Response<List<K8sInstanceVO>> findAllK8sInstances() throws ApiException {
        List<K8sInstanceVO> allK8sInstances = instanceService.findAllK8sInstances();
        return Response.success(allK8sInstances);
    }

    @RequestMapping(value = "/cloud", method = RequestMethod.GET)
    public Response<PageVO<InstanceEntity>> findCloudInstances(@RequestParam(required = false) String env,
                                                               @RequestParam(required = false) String appId,
                                                               @RequestParam(required = false) String name,
                                                               @RequestParam(required = false) String ip,
                                                               @RequestParam(value = "page") Integer page,
                                                               @RequestParam(value = "size") Integer size) {
        PageVO<InstanceEntity> instancePageVO = instanceService.findCloudInstancesByCondition(env, appId, name, ip, page, size);
        return Response.success(instancePageVO);
    }

    /**
     * @return 返回PaaS发布支持的实例规格列表
     */
    @RequestMapping(value = "/specs", method = RequestMethod.GET)
    public Response<List<InstanceSpecVO>> listSpecs() {
        List<InstanceSpecVO> specVOs = taskService.readInstanceSpecs();
        return Response.mark(MessageType.SUCCESS, specVOs);
    }

    /**
     * 查询容器日志
     * @param instanceName
     * @return
     */
    @RequestMapping(value = "/containerlog", method = RequestMethod.GET)
    public Response<ContainerLogVO> getContainerLog(@RequestParam(value = "instanceName") String instanceName) {
        GetInstanceLogRequest getInstanceLogRequest = new GetInstanceLogRequest();
        getInstanceLogRequest.setName(instanceName);
        GetInstanceLogResponse getInstanceLogResponse = cloudInstanceService.log(getInstanceLogRequest);

        ContainerLogVO containerLogVO = new ContainerLogVO();
        containerLogVO.setLogs(getInstanceLogResponse.getLog());
        return Response.success(containerLogVO);
    }

    @RequestMapping(value = "/condition", method = RequestMethod.GET)
    public Response<List<InstanceVO>> findInstancesByCondition(@RequestParam(value = "appId", required = false) String appId,
                                                                 @RequestParam(value = "env", required = false) String env,
                                                                 @RequestParam(value = "ip", required = false) String ip) {
        List<InstanceVO> instances = new ArrayList<>();
        if (StringUtils.isNotEmpty(ip)) {
            instances = instanceService.getInstanceStatusByIp(ip);
        } else if (StringUtils.isNotEmpty(appId) && StringUtils.isNotEmpty(env)) {
            instances = instanceService.getInstanceStatusByAppIdAndEnv(appId, env);
        }
        return Response.mark(MessageType.SUCCESS, instances);
    }

    @RequestMapping(value = "/host", method = RequestMethod.GET)
    public Response<List<InstanceEntity>> findInstancesByHost(@RequestParam String env,
                                                              @RequestParam String hostIp) {
        List<InstanceEntity> instances = instanceService.findInstancesByHostIp(env.trim(), hostIp.trim());
        return Response.success(instances);
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public ResponseEntity<Resource> export(@RequestParam String env,
                                           @RequestParam String hostIp) {
        Map<String, ApplicationEntity> applicationEntityMap = new HashMap<>();
        List<InstanceEntity> instances = instanceService.findInstancesByHostIp(env.trim(), hostIp.trim());

        StringBuilder sb = new StringBuilder();

        for (InstanceEntity instanceEntity : instances) {
            ApplicationEntity applicationEntity = applicationEntityMap.get(instanceEntity.getAppId());
            if (applicationEntity == null) {
                applicationEntity = appService.getAppByCmdbId(instanceEntity.getAppId());
                applicationEntityMap.put(instanceEntity.getAppId(), applicationEntity);
            }

            sb.append(applicationEntity.getName()).append(", ")
                    .append(instanceEntity.getSlotIp())
                    .append(", ")
                    .append(applicationEntity.getDepartment())
                    .append(", \"")
                    .append(applicationEntity.getOwner())
                    .append("\"\n");
        }

        String content = sb.toString();
        byte[] contentBytes = content.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"instances.csv\""));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(contentBytes.length)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new ByteArrayResource(contentBytes));
    }

    @RequestMapping(value = "/exec", method = RequestMethod.POST)
    public Response<ExecCommandResultVO> exec(@RequestBody ExecCommandVO execCommandVO) {
        if (execCommandVO.getType().toLowerCase().equals("curl")) {
            ExecCommandResultVO execCommandResultVO = execCommandService.execCurl(execCommandVO.getInstance(), execCommandVO.getArgs());
            return Response.success(execCommandResultVO);
        } else if (execCommandVO.getType().toLowerCase().equals("ping")) {
            ExecCommandResultVO execCommandResultVO = execCommandService.execPing(execCommandVO.getInstance(), execCommandVO.getArgs());
            return Response.success(execCommandResultVO);
        }

        return Response.success(new ExecCommandResultVO());
    }

    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public Response<String> transferInstances(@RequestBody TransferInstanceVO transferInstanceVO) {
        if (StringUtils.isEmpty(transferInstanceVO.getEnv())) {
            return Response.error("env不能为空");
        }

        if (StringUtils.isEmpty(transferInstanceVO.getHostIp())) {
            return Response.error("hostIp不能为空");
        }

        if (transferInstanceVO.getInstanceIds() == null || transferInstanceVO.getInstanceIds().isEmpty()) {
            return Response.error("instanceIds不能为空");
        }

        instanceService.transferInstances(transferInstanceVO);

        return Response.success("迁移实例任务开始执行");
    }

    @RequestMapping(value = "/env/{env}", method = RequestMethod.GET)
    public Response<List<EnvInstanceVO>> findInstancesByEnv(@PathVariable String env) {
        List<EnvInstanceVO> envInstanceVOList = instanceService.getInstancesByEnv(env);
        return Response.success(envInstanceVOList);
    }

    @RequestMapping(value = "/app", method = RequestMethod.GET)
    public Response<List<InstanceEntity>> findAppInstances(@RequestParam(value = "appId") String appId,
                                                           @RequestParam(value = "env", required = false) String env) {
        List<InstanceEntity> instances;
        if (env == null) {
            instances = instanceService.findByAppId(appId);
        } else {
            instances = instanceService.findByEnvAndAppId(env, appId);
        }
        return Response.success(instances);
    }

    @RequestMapping(value = "/healthcheck", method = RequestMethod.GET)
    public Response<String> doHealthCheck(@RequestParam("ip") String ip,
                                          @RequestParam("port") String port) {
        String url = "http://" + ip + ":" + port + "/hs";
        try {
            String response = hcHttpClient.get(url);
            return Response.success(response);
        } catch (IOException e) {
            return Response.mark(MessageType.ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public Response<List<InstanceCountVO>> getInstanceCountByZone() {
        List<InstanceCountVO> instanceCountVOList = instanceService.getInstanceCountByZone();
        return Response.success(instanceCountVOList);
    }
}
