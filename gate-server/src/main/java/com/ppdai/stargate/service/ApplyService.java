package com.ppdai.stargate.service;

import com.alibaba.fastjson.JSON;
import com.ppdai.atlas.client.api.ApplyControllerApi;
import com.ppdai.atlas.client.invoker.ApiException;
import com.ppdai.atlas.client.model.ApplyDto;
import com.ppdai.atlas.client.model.ResponsePageVOApplyDto;
import com.ppdai.auth.common.identity.Identity;
import com.ppdai.auth.utils.PauthTokenUtil;
import com.ppdai.infrastructure.paas.apply.api.*;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.vi.ApplyNewAppVI;
import com.ppdai.stargate.vi.ChangeAppQuotaVI;
import com.ppdai.stargate.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApplyService {

    @Autowired
    private ApplyControllerApi atlasApplyControllerApi;

    @Autowired
    private AppService appService;

    @Autowired
    private PauthTokenUtil pauthTokenUtil;

    public void newApp(ApplyNewAppVI applyNewAppVI) {
        Identity identity = pauthTokenUtil.getTokenInfo();

        ApplyDto apply = new ApplyDto();
        apply.setApplyUser(identity.getName());
        apply.setApplyDepartment(identity.getOrganzation());
        apply.setType(ApplyType.NEW_APP.name());
        apply.setStatus(ApplyStatus.NEW.name());
        apply.setRequest(JSON.toJSONString(applyNewAppVI));

        try {
            atlasApplyControllerApi.createApplyUsingPOST(apply);
        } catch (ApiException e) {
            log.error(e.getMessage(), e);
            throw BaseException.newException(MessageType.ERROR, "向atlas提交应用申请失败: err=" + e.getMessage() + ", responsebody=" + e.getResponseBody());
        }
    }

    public void changeAppQuota(ChangeAppQuotaVI changeAppQuotaVI) {
        Identity identity = pauthTokenUtil.getTokenInfo();

        ApplyDto apply = new ApplyDto();
        apply.setApplyUser(identity.getName());
        apply.setApplyDepartment(identity.getOrganzation());
        apply.setType(ApplyType.CHANGE_QUOTA.name());
        apply.setStatus(ApplyStatus.NEW.name());

        ApplicationEntity applicationEntity = appService.getAppByCmdbId(changeAppQuotaVI.getAppId());
        changeAppQuotaVI.setAppName(applicationEntity.getName());

        apply.setRequest(JSON.toJSONString(changeAppQuotaVI));

        try {
            atlasApplyControllerApi.createApplyUsingPOST(apply);
        } catch (ApiException e) {
            log.error(e.getMessage(), e);
            throw BaseException.newException(MessageType.ERROR, "向atlas配额修改申请失败: err=" + e.getMessage() + ", responsebody=" + e.getResponseBody());
        }
    }

    public PageVO<ApplyDto> getByPage(String applyUser, String status, int page, int size) throws ApiException {
        ResponsePageVOApplyDto responsePageVOApplyDto = atlasApplyControllerApi.queryByPageUsingGET(page - 1, size, applyUser, status);
        PageVO<ApplyDto> applyPageVO = new PageVO<>();
        applyPageVO.setContent(responsePageVOApplyDto.getDetails().getContent());
        applyPageVO.setTotalElements(responsePageVOApplyDto.getDetails().getTotalElements());
        return applyPageVO;
    }
}
