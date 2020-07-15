package com.ppdai.stargate.controller;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.po.ResourceEntity;
import com.ppdai.stargate.service.ResourceService;
import com.ppdai.stargate.vi.AddStaticResourceVI;
import com.ppdai.stargate.vo.PageVO;
import com.ppdai.stargate.vo.ResourceQuotaStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@Slf4j
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @RequestMapping(value = "/ip", method = RequestMethod.GET)
    public Response<String> findIpByPodName(@RequestParam String podname) {
        String ip = resourceService.findIpByPodName(podname);
        return Response.success(ip);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Response<String> addStaticResource(@RequestBody AddStaticResourceVI addStaticResourceVI) {
        String appId = addStaticResourceVI.getAppId();
        String env = addStaticResourceVI.getEnv();
        String spec = addStaticResourceVI.getSpec();
        Integer number = addStaticResourceVI.getNumber();
        String zone = addStaticResourceVI.getZone();
        resourceService.addStaticResource(appId, env, spec, number, zone);
        return Response.success("静态资源添加成功");
    }

    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Response<PageVO<ResourceEntity>> findResources(@RequestParam(required = false) String appId,
                                                          @RequestParam(required = false) String env,
                                                          @RequestParam(required = false) String spec,
                                                          @RequestParam(required = false) String ip,
                                                          @RequestParam(required = false) Boolean isStatic,
                                                          @RequestParam Integer page,
                                                          @RequestParam Integer size) {
        PageVO<ResourceEntity> resourcePageVO = resourceService.findResourcesByPage(appId, env, spec, ip, isStatic, page, size);
        return Response.success(resourcePageVO);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Response<String> removeResource(@PathVariable("id") Long id) {
        resourceService.removeResourceById(id);
        return Response.success("资源删除成功");
    }

    @RequestMapping(value = "/quotas/status", method = RequestMethod.GET)
    public Response<List<ResourceQuotaStatusVO>> fetchResourceQuotaStatus(@RequestParam String appId,
                                                                          @RequestParam String env) {
        List<ResourceQuotaStatusVO> resourceQuotaStatusVOS = resourceService.fetchResourceQuotaStatus(appId, env);
        return Response.success(resourceQuotaStatusVOS);
    }

    @RequestMapping(value = "/available", method = RequestMethod.GET)
    public Response<List<ResourceEntity>> findAvailableStaticResources(@RequestParam String appId,
                                                                       @RequestParam String env,
                                                                       @RequestParam String spec) {
        List<ResourceEntity> resources = resourceService.findAvailableStaticResources(appId, env, spec);
        return Response.success(resources);
    }

    @RequestMapping(value = "/changeType", method = RequestMethod.POST)
    public Response<String> changeTypeForAllResources(@RequestParam String appId,
                                                      @RequestParam String env,
                                                      @RequestParam boolean isStatic) {
        int count = resourceService.changeTypeForAllResources(appId, env, isStatic);
        return Response.mark(MessageType.SUCCESS, "资源类型转换完成, 数量 = %d", count);
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public Response<String> initResources() {
        resourceService.initResources();
        return Response.success("资源初始化成功");
    }
}