package com.ppdai.stargate.controller;

import com.ppdai.atlas.client.model.AppQuotaDto;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.service.AppService;
import com.ppdai.stargate.vi.UpdateAppMemberVI;
import com.ppdai.stargate.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/apps")
@Slf4j
public class AppController {

    @Autowired
    private AppService appService;

    @RequestMapping(method = RequestMethod.GET)
    public Response<List<AppVO>> findApps(@RequestParam(value = "appId", required = false) String appId,
                                          @RequestParam(value = "department", required = false) String department,
                                          @RequestParam(value = "username", required = false) String username) {
        List<AppVO> apps;
        if (appId == null && department == null && username == null) {
            apps = appService.fetchAllAppsFromLocalDB();
        } else if (appId != null) {
            apps = Arrays.asList(appService.fetchAppByAppId(appId));
        } else if (username != null) {
            apps = appService.fetchAppsByUsername(username);
        } else {
            apps = appService.fetchAppsByDepartmentFromLocalDB(department);
        }
        return Response.mark(MessageType.SUCCESS, apps);
    }

    @RequestMapping(value = "/condition", method = RequestMethod.GET)
    public Response<PageVO<ApplicationEntity>> fetchAppListByPage(@RequestParam(required = false) String appId,
                                                                  @RequestParam Integer page,
                                                                  @RequestParam Integer size) {
        PageVO<ApplicationEntity> appPageVO = appService.fetchAppsByPage(page, size, appId);
        return Response.mark(MessageType.SUCCESS, appPageVO);
    }

    @RequestMapping(value = "/sync", method = RequestMethod.POST)
    public Response<String> SyncApps() {
        Boolean bSuccess = appService.syncAllApps();
        String msg = bSuccess ? "同步数据成功。" : "同步数据失败。";
        MessageType msgType = bSuccess ? MessageType.SUCCESS : MessageType.ERROR;
        return Response.mark(msgType, msg);
    }

    @RequestMapping(value = "/quotas", method = RequestMethod.GET)
    public Response<List<AppQuotaDto>> fetchAppQuotas(@RequestParam String appId,
                                                      @RequestParam String environment) {
        List<AppQuotaDto> appQuotas = appService.fetchAppQuotasByAppAndEnv(appId, environment);
        return Response.mark(MessageType.SUCCESS, appQuotas);
    }

    @RequestMapping(value = "/quotas/status", method = RequestMethod.GET)
    public Response<List<AppQuotaStatusVO>> fetchAppQuotaStatus(@RequestParam String appId,
                                                      @RequestParam String environment) {
        List<AppQuotaStatusVO> appQuotaStatusVOS = appService.fetchAppQuotaStatusByAppAndEnv(appId, environment);
        return Response.mark(MessageType.SUCCESS, appQuotaStatusVOS);
    }

    @RequestMapping(value = "/member", method = RequestMethod.POST)
    public Response<String> updateAppMember(@RequestBody UpdateAppMemberVI updateAppMemberVI) {
        String appId = updateAppMemberVI.getAppId();
        String developers = updateAppMemberVI.getDevelopers();
        String testers = updateAppMemberVI.getTesters();
        Boolean isSuccess = appService.updateAppMember(appId, developers, testers);
        if (isSuccess != null && isSuccess.equals(Boolean.TRUE)) {
            return Response.success("更新成功");
        } else {
            return Response.error("更新失败，请输入正确的域账号，用英文逗号分隔");
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Response<String> removeApp(@PathVariable("id") Long id) {
        appService.removeAppById(id);
        return Response.success("应用删除成功");
    }

    @RequestMapping(value = "/site", method = RequestMethod.GET)
    public Response<SiteStatusVO> fetchSiteStatus(@RequestParam String env,
                                                  @RequestParam String appId) {
        SiteStatusVO siteStatusVO = appService.fetchSiteStatus(env, appId);
        return Response.success(siteStatusVO);
    }

}
