package com.ppdai.stargate.job.task.handler.group;

import com.ppdai.stargate.constant.JobStatus;
import com.ppdai.stargate.constant.JobTaskTypeEnum;
import com.ppdai.stargate.constant.TaskStatus;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.AbstractTaskHandler;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.manager.JobManager;
import com.ppdai.stargate.manager.TaskManager;
import com.ppdai.stargate.po.GroupEntity;
import com.ppdai.stargate.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class SyncInstanceJobTaskHandler extends AbstractTaskHandler {

    @Autowired
    private GroupService groupService;

    @Autowired
    private JobManager jobManager;

    @Autowired
    private TaskManager taskManager;

    @Override
    public String getName() {
        return JobTaskTypeEnum.SYNC_INSTANCE_JOB.name();
    }

    private void restartFailedJobs(String[] jobIdList) {
        for (String v : jobIdList) {
            long jobId = Long.parseLong(v);
            Optional<JobInfo> jobInfoOp = jobManager.getJobInfoById(jobId);
            if (!jobInfoOp.isPresent()) {
                log.error("<<SyncInstanceJobTaskHandler>> 没有找到指定的job, id=" + jobId);
                continue;
            }

            JobInfo ji = jobInfoOp.get();
            if (ji.getStatus().equals(JobStatus.FAIL)) {
                List<TaskInfo> taskInfoList = taskManager.getTaskInfosByJobId(jobId);
                for (TaskInfo ti : taskInfoList) {
                    if (ti.getStatus().equals(TaskStatus.FAIL)) {
                        ti.setStatus(TaskStatus.NEW);
                        taskManager.updateTask(ti);
                    }
                }

                ji.setStatus(JobStatus.NEW);
                jobManager.updateJob(ji);
            }
        }
    }

    private void waitJobs(String[] jobIdList) throws InterruptedException {
        for (String v : jobIdList) {
            long jobId = Long.parseLong(v);

            Optional<JobInfo> jobInfoOp = jobManager.getJobInfoById(jobId);
            if (!jobInfoOp.isPresent()) {
                log.error("<<SyncInstanceJobTaskHandler>> 没有找到指定的job, id=" + jobId);
                continue;
            }

            JobInfo ji = jobInfoOp.get();
            while (ji.getStatus().equals(JobStatus.NEW) || ji.getStatus().equals(JobStatus.RUNNING)) {
                log.info("<<SyncInstanceJobTaskHandler>> job未执行完, 等待1s, id=" + jobId);
                Thread.sleep(1000);
                ji = jobManager.getJobInfoById(jobId).get();
            }

            if (!ji.getStatus().equals(JobStatus.SUCCESS)) {
                BaseException ex = BaseException.newException(MessageType.ERROR, "实例任务失败，jobId=" + ji.getId()
                        + ", instance=" + ji.getName() + ", reason=" + ji.getAdditionalInfo());
                log.error("<<SyncInstanceJobTaskHandler>> " + ex.getMessage());
                throw ex;
            }
        }
    }

    @Override
    public void execute(TaskInfo taskInfo) throws Exception {
        JobInfo jobInfo = taskInfo.getJobInfo();
        log.info("<<SyncInstanceJobTaskHandler>> 开始同步实例任务: taskId={}, jobId={}, groupId={}",
                taskInfo.getId(), jobInfo.getId(), jobInfo.getGroupId());

        Long groupId = jobInfo.getGroupId();
        GroupEntity group = groupService.getGroupById(groupId);
        if (group == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定的发布组不存在，groupId = " + groupId);
            log.error("<<SyncInstanceJobTaskHandler>> " + ex.getMessage());
            throw ex;
        }

        Object value = taskInfo.getDataMap().get("jobIdList");
        if (value == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定的jobIdList，groupId = " + groupId);
            log.error("<<SyncInstanceJobTaskHandler>> " + ex.getMessage());
            throw ex;
        }

        String[] jobIdList = value.toString().split(",");
        restartFailedJobs(jobIdList);
        waitJobs(jobIdList);
    }
}
