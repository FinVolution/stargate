package com.ppdai.stargate.service;

import java.util.*;
import javax.persistence.criteria.Predicate;

import com.ppdai.auth.utils.PauthTokenUtil;
import com.ppdai.stargate.constant.*;
import com.ppdai.stargate.dao.TaskRepository;
import com.ppdai.stargate.manager.TaskManager;
import com.ppdai.stargate.po.*;
import com.ppdai.stargate.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.dao.JobRepository;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.manager.JobManager;
import com.ppdai.stargate.utils.ConvertUtil;
import org.springframework.util.StringUtils;

@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    @Autowired
    private JobManager jobManager;
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private JobRepository jobRepo;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private GroupService groupService;
    @Autowired
    private EnvService envService;
    @Autowired
    private PauthTokenUtil pauthTokenUtil;
    @Autowired
    private Environment environment;

    /**
     * 同步发布组内实例任务状态
     * @param operatorType
     * @param groupId
     * @param jobIdList
     * @return
     */
    public JobInfo addSyncInstanceJob(String operatorType, Long groupId, List<Long> jobIdList) {
        GroupEntity groupEntity = groupService.getGroupById(groupId);
        if (groupEntity == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定发布组[%s]不存在。", groupId);
            logger.error("<<addSyncInstanceJob>> " + ex.getMessage());
            throw ex;
        }

        Map<String, Object> data = Maps.newHashMap();
        String token = pauthTokenUtil.getToken();
        if (token == null) {
            token = environment.getProperty("stargate.job.token", "");
        }
        data.put("token", token);

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(groupEntity.getName());
        jobInfo.setEnv(groupEntity.getEnvironment());
        jobInfo.setAppId(groupEntity.getAppId());
        jobInfo.setAppName(groupEntity.getAppName());
        jobInfo.setGroupId(groupId);
        jobInfo.setOperationType(operatorType);
        jobInfo.setDataMap(data);

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;

        if (jobIdList.size() > 0) {
            Map<String, Object> taskData = Maps.newHashMap();
            taskData.put("jobIdList", org.apache.commons.lang.StringUtils.join(jobIdList, ","));
            taskList.add(new TaskInfo(step++, JobTaskTypeEnum.SYNC_INSTANCE_JOB, taskData));
        }
        if (operatorType.equals(OperatorType.REMOVE_GROUP.name())) {
            taskList.add(new TaskInfo(step++, JobTaskTypeEnum.removeGroup));
        }
        return jobManager.createJobWithTasks(jobInfo);
    }

    public void restartJobById(Long jobId) {
        JobEntity job = jobRepo.findOne(jobId);
        if (job == null) {
            throw BaseException.newException(MessageType.ERROR, "任务不存在, jobId=" + jobId);
        }
        if (job.getStatus() == JobStatus.SUCCESS) {
            throw BaseException.newException(MessageType.ERROR, "任务已经执行成功, jobId=" + jobId);
        }
        if (job.getStatus() == JobStatus.NEW || job.getStatus() == JobStatus.RUNNING) {
            throw BaseException.newException(MessageType.ERROR, "任务正在执行中, jobId=" + jobId);
        }

        List<TaskEntity> tasks = taskRepository.findTasksByJobId(jobId);
        tasks.forEach(task -> {
            TaskStatus taskStatus = task.getStatus();
            if (taskStatus == TaskStatus.EXPIRED || taskStatus == TaskStatus.FAIL) {
                task.setStatus(TaskStatus.NEW);
                task.setAdditionalInfo(null);
                taskRepository.save(task);
            }
        });

        job.setStatus(JobStatus.NEW);
        job.setAdditionalInfo(null);
        jobRepo.save(job);
    }

    public JobEntity findById(Long jobId) {
        return jobRepo.findOne(jobId);
    }

    public PageVO<JobVO> getJobsByPage(String env, String appId, Long groupId, String operationType, String status, String startTime, String endTime, Integer page, Integer size) {
        PageVO<JobVO> jobPageVO = new PageVO<>();
        Pageable pageable = new PageRequest(page - 1, size);

        Page<JobEntity> jobPage = jobRepo.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (!StringUtils.isEmpty(env)) {
                list.add(criteriaBuilder.equal(root.get("env").as(String.class), env));
            }
            if (!StringUtils.isEmpty(appId)) {
                list.add(criteriaBuilder.equal(root.get("appId").as(String.class), appId));
            }
            if (groupId != null) {
                list.add(criteriaBuilder.equal(root.get("groupId").as(Long.class), groupId));
            }
            if (!StringUtils.isEmpty(operationType)) {
                list.add(criteriaBuilder.equal(root.get("operationType").as(String.class), operationType));
            }
            if (!StringUtils.isEmpty(status)) {
                list.add(criteriaBuilder.equal(root.get("status").as(String.class), status));
            }
            if (!StringUtils.isEmpty(startTime)) {
                Date stime = new Date(Long.valueOf(startTime));
                list.add(criteriaBuilder.greaterThanOrEqualTo(root.get("insertTime").as(Date.class), stime));
            }
            if (!StringUtils.isEmpty(endTime)) {
                Date etime = new Date(Long.valueOf(endTime));
                list.add(criteriaBuilder.lessThanOrEqualTo(root.get("insertTime").as(Date.class), etime));
            }
            list.add(criteriaBuilder.equal(root.get("isActive").as(Boolean.class), Boolean.TRUE));
            Predicate[] p = new Predicate[list.size()];
            criteriaQuery.orderBy(criteriaBuilder.desc(root.get("id").as(Long.class)));
            return criteriaBuilder.and(list.toArray(p));
        }, pageable);

        List<JobVO> jobVOList = new ArrayList<>();
        List<JobEntity> jobList = jobPage.getContent();
        jobList.forEach((job) -> {
            JobVO jobVO = ConvertUtil.convert(job, JobVO.class);
            jobVO.setOperationTypeDesc(OperatorType.valueOf(jobVO.getOperationType()).getDescription());

            List<TaskInfo> taskList = taskManager.getTaskInfosByJobId(job.getId());
            jobVO.setTaskList(taskList);

            jobVOList.add(jobVO);
        });

        jobPageVO.setContent(jobVOList);
        jobPageVO.setTotalElements(jobPage.getTotalElements());
        return jobPageVO;
    }

    public long getJobCountByEnvAndStatus(String env, JobStatus jobStatus) {
        long jobCount = jobRepo.count((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (!StringUtils.isEmpty(env)) {
                list.add(criteriaBuilder.equal(root.get("env").as(String.class), env));
            }
            if (jobStatus != null) {
                list.add(criteriaBuilder.equal(root.get("status").as(JobStatus.class), jobStatus));
            }
            list.add(criteriaBuilder.equal(root.get("isActive").as(Boolean.class), Boolean.TRUE));
            Predicate[] p = new Predicate[list.size()];
            return criteriaBuilder.and(list.toArray(p));
        });
        return jobCount;
    }

    public List<JobStatusVO> getJobStatus() {
        List<JobStatusVO> jobStatusList = new ArrayList<>();
        List<EnvVO> envList = envService.getInUseEnvironments();
        envList.forEach(env -> {
            JobStatusVO jobStatus = new JobStatusVO();
            long runningCount = getJobCountByEnvAndStatus(env.getName(), JobStatus.RUNNING);

            long successCount = getJobCountByEnvAndStatus(env.getName(), JobStatus.SUCCESS);
            long failCount = getJobCountByEnvAndStatus(env.getName(), JobStatus.FAIL);
            failCount += getJobCountByEnvAndStatus(env.getName(), JobStatus.EXPIRED);
            jobStatus.setEnvironment(env.getName());
            jobStatus.setRunningCount(runningCount);
            jobStatus.setSuccessCount(successCount);
            jobStatus.setFailCount(failCount);
            jobStatusList.add(jobStatus);
        });
        return jobStatusList;
    }

    public JobInfo getCurrentJobByGroupId(Long groupId) {
        return this.jobManager.getCurrentJobByGroupId(groupId);
    }

    public List<JobEntity> findRecentUsedImages(String env, String appId) {
        return jobRepo.findRecentUsedImages(env, appId,
                Arrays.asList(OperatorType.CREATE_GROUP.name(), OperatorType.EXPAND_GROUP.name(), OperatorType.UPDATE_INSTANCE.name(), OperatorType.ROLLING_GROUP.name()),
                new PageRequest(0, 10));
    }

}
