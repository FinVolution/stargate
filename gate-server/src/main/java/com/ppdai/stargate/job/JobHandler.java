package com.ppdai.stargate.job;

import com.ppdai.stargate.constant.JobStatus;
import com.ppdai.stargate.constant.JobTypeEnum;
import com.ppdai.stargate.constant.TaskStatus;
import com.ppdai.stargate.job.task.TaskExecutor;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.manager.JobManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobHandler implements IHandler<JobInfo> {

    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private JobManager jobManager;

    @Override
    public String getName() {
        return JobTypeEnum.defaultType.name();
    }

    @Override
    public void execute(JobInfo jobInfo) throws Exception {
        for (TaskInfo taskInfo : jobInfo.getTaskInfos()) {

            log.info("开始处理任务, taskName={}, status={}", taskInfo.getName(), taskInfo.getStatus());
            if (!taskInfo.getStatus().equals(TaskStatus.SUCCESS)) {
                taskInfo.setJobInfo(jobInfo);
                taskExecutor.executeTask(taskInfo);
            }
            log.info("任务执行完成, taskName={}, status={}", taskInfo.getName(), taskInfo.getStatus());
        }
    }

    @Override
    public void onSuccess(JobInfo jobInfo) {
        jobInfo.setStatus(JobStatus.SUCCESS);
        jobManager.updateJob(jobInfo);
        log.info("<<JobHandler>> jobId={}, status={}", jobInfo.getId(), jobInfo.getStatus());
    }

    @Override
    public void onFail(JobInfo jobInfo) {
        jobInfo.setStatus(JobStatus.FAIL);
        jobManager.updateJob(jobInfo);
        log.info("<<JobHandler>> jobId={}, status={}", jobInfo.getId(), jobInfo.getStatus());
    }

    @Override
    public void onExpire(JobInfo jobInfo) {
        jobInfo.setStatus(JobStatus.EXPIRED);
        jobManager.updateJob(jobInfo);
        log.info("<<JobHandler>> jobId={}, status={}", jobInfo.getId(), jobInfo.getStatus());
    }

    @Override
    public void onInterrupt(JobInfo jobInfo) {
        jobInfo.setStatus(JobStatus.FAIL);
        jobManager.updateJob(jobInfo);
        log.info("<<JobHandler>> jobId={}, status={}", jobInfo.getId(), jobInfo.getStatus());
    }
}
