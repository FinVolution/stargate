package com.ppdai.stargate.controller;

import com.ppdai.atlas.client.model.ApplyDto;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.service.ApplyService;
import com.ppdai.stargate.vi.ApplyNewAppVI;
import com.ppdai.stargate.vi.ChangeAppQuotaVI;
import com.ppdai.stargate.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applies")
@Slf4j
public class ApplyController {

    @Autowired
    private ApplyService applyService;

    @RequestMapping(value = "/newApp", method = RequestMethod.POST)
    public Response<String> newApp(@RequestBody ApplyNewAppVI applyNewAppVI) {
        try {
            applyService.newApp(applyNewAppVI);
            return Response.mark(MessageType.SUCCESS, "您已成功提交申请，请耐心等待。您可以在申请历史查看申请结果。");
        }  catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Response.mark(MessageType.ERROR, "提交申请失败");
        }
    }

    @RequestMapping(value = "/changeAppQuota", method = RequestMethod.POST)
    public Response<String> changeAppQuota(@RequestBody ChangeAppQuotaVI changeAppQuotaVI) {
        try {
            applyService.changeAppQuota(changeAppQuotaVI);
            return Response.mark(MessageType.SUCCESS, "您已成功提交申请，请耐心等待。您可以在申请历史查看申请结果。");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Response.mark(MessageType.ERROR, "提交申请失败");
        }
    }

    @RequestMapping(value = "/queryAppliesByPage", method = RequestMethod.GET)
    public Response<PageVO<ApplyDto>> queryByPage(@RequestParam(value = "applyUser", required = false) String applyUser,
                                               @RequestParam(value = "status", required = false) String status,
                                               @RequestParam(value = "page") Integer page,
                                               @RequestParam(value = "size") Integer size) {


        try {
            PageVO<ApplyDto> applyPageVO = applyService.getByPage(applyUser, status, page, size);
            return Response.mark(MessageType.SUCCESS, applyPageVO);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Response.mark(MessageType.ERROR, new PageVO());
        }

    }
}
