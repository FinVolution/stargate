package com.ppdai.stargate.controller.api;

import com.ppdai.stargate.constant.OperatorType;
import com.ppdai.stargate.dto.GetJobRequest;
import com.ppdai.stargate.dto.GetJobResponse;
import com.ppdai.stargate.dto.RestartJobRequest;
import com.ppdai.stargate.dto.RestartJobResponse;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.manager.TaskManager;
import com.ppdai.stargate.po.JobEntity;
import com.ppdai.stargate.service.JobService;
import com.ppdai.stargate.utils.ConvertUtil;
import com.ppdai.stargate.vo.JobVO;
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

import java.util.List;

@Api(description = "提供任务相关的一些操作")
@RestController
@RequestMapping("/api/cloud/job")
@Slf4j
public class JobApiController {

    @Autowired
    private JobService jobService;

    @Autowired
    private TaskManager taskManager;

    @ApiOperation(value = "查询任务", notes = "查询任务", response = GetJobResponse.class)
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    GetJobResponse get(@RequestBody GetJobRequest request) {
        try {
            if (request.getJobId() == null) {
                return new GetJobResponse(-1, "参数[jobId]为空");
            }

            JobEntity jobEntity = jobService.findById(request.getJobId());

            if (jobEntity == null) {
                return new GetJobResponse(-1, "无法找到job, jobId=" + request.getJobId());
            }

            JobVO jobVO = ConvertUtil.convert(jobEntity, JobVO.class);

            jobVO.setOperationTypeDesc(OperatorType.valueOf(jobVO.getOperationType()).getDescription());

            List<TaskInfo> taskList = taskManager.getTaskInfosByJobId(request.getJobId());
            jobVO.setTaskList(taskList);

            GetJobResponse response = new GetJobResponse(0, "success");
            response.setJob(jobVO);
            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new GetJobResponse(-1, ex.getMessage());
        }
    }

    @ApiOperation(value = "重新执行任务", notes = "重新执行任务", response = RestartJobResponse.class)
    @RequestMapping(value = "/restart", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "正常返回"),
            @ApiResponse(code = 500, message = "系统异常") })
    RestartJobResponse restart(@RequestBody RestartJobRequest request) {
        try {
            if (request.getJobId() == null) {
                return new RestartJobResponse(-1, "参数[jobId]为空");
            }

            JobEntity jobEntity = jobService.findById(request.getJobId());

            if (jobEntity == null) {
                return new RestartJobResponse(-1, "无法找到job, jobId=" + request.getJobId());
            }

            JobVO jobVO = ConvertUtil.convert(jobEntity, JobVO.class);

            jobVO.setOperationTypeDesc(OperatorType.valueOf(jobVO.getOperationType()).getDescription());

            List<TaskInfo> taskList = taskManager.getTaskInfosByJobId(request.getJobId());
            jobVO.setTaskList(taskList);

            jobService.restartJobById(request.getJobId());

            RestartJobResponse response = new RestartJobResponse(0, "success");
            response.setJob(jobVO);
            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new RestartJobResponse(-1, ex.getMessage());
        }
    }
}
