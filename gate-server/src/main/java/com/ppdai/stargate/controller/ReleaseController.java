package com.ppdai.stargate.controller;

import com.ppdai.stargate.constant.OperatorType;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.service.JobService;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/releases")
@Slf4j
public class ReleaseController {

	@Autowired
	private JobService jobService;

	@RequestMapping(value = "/condition", method = RequestMethod.GET)
    public Response<PageVO<JobVO>> fetchJobsByPage(@RequestParam(value = "env", required = false) String env,
                                                   @RequestParam(value = "appId", required = false) String appId,
                                                   @RequestParam(value = "groupId", required = false) Long groupId,
                                                   @RequestParam(value = "operationType", required = false) String operationType,
                                                   @RequestParam(value = "status", required = false) String status,
                                                   @RequestParam(value = "startTime", required = false) String startTime,
                                                   @RequestParam(value = "endTime", required = false) String endTime,
                                                   @RequestParam(value = "page") Integer page,
                                                   @RequestParam(value = "size") Integer size) {
        PageVO<JobVO> jobPageVO = jobService.getJobsByPage(env, appId, groupId, operationType, status, startTime, endTime, page, size);
        return Response.mark(MessageType.SUCCESS, jobPageVO);
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public Response<List<JobStatusVO>> queryJobStatus() {
        List<JobStatusVO> jobStatusList = jobService.getJobStatus();
        return Response.mark(MessageType.SUCCESS, jobStatusList);
    }

    @RequestMapping(value = "/types", method = RequestMethod.GET)
    public Response<List<OperationTypeVO>> fetchAllOperationTypes() {
        List<OperationTypeVO> operationTypeVOList = new ArrayList<>();
        for (OperatorType type : OperatorType.values()) {
            OperationTypeVO operationTypeVO = new OperationTypeVO();
            operationTypeVO.setName(type.name());
            operationTypeVO.setDescription(type.getDescription());
            operationTypeVOList.add(operationTypeVO);
        }
        return Response.success(operationTypeVOList);
    }
	
}
