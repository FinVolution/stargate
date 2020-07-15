package com.ppdai.stargate.job;

import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.manager.JobManager;
import com.ppdai.stargate.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class JobMonitor {

    @Autowired
    private JobManager jobManager;
    @Autowired
    private JobExecutor jobExecutor;

    public void processNewJob() {
        if (jobExecutor.isFullLoad()) {
            log.warn("处理job的队列已满, job延迟执行!");
            return;
        }

        //扫描新job
        List<JobInfo> jobInfos = jobManager.getNewJob();
        jobInfos.forEach(jobInfo -> {
            //获取锁，提交job执行器
            MDC.put("Group", jobInfo.getName());
            MDC.put("GroupJob", jobInfo.getName() + "/" + String.valueOf(jobInfo.getId()));

            log.info("开始处理job, jobId={}", jobInfo.getId());

            try {
                if (jobManager.lock(jobInfo)) {
                    log.info("获取到job锁,开始获取任务: jobId={}, ip={}", jobInfo.getId(), IPUtil.localIP());
                    jobManager.fetchJobTask(jobInfo);

                    log.info("获取任务完成: 任务数={}", jobInfo.getTaskInfos().size());
                    for (TaskInfo taskInfo : jobInfo.getTaskInfos()) {
                        log.info("任务: name={}", taskInfo.getName());
                    }

                    jobExecutor.submitJob(jobInfo);
                } else {
                    log.info("法获取到job锁, 丢弃job: jobId={}, ip={}", jobInfo.getId(), IPUtil.localIP());
                }
            } catch (Exception ex) {
                log.error("提交Job处理失败: err=" + ex.getMessage(), ex);
            } finally {
                MDC.remove("Group");
                MDC.remove("GroupJob");
            }
        });
    }

    /**
     * 检查是否有执行超时的job
     */
    public void processExpiredJob(){
        List<JobInfo> jobInfos = jobManager.getRunningJob();
        jobInfos.forEach(jobInfo -> {
            jobExecutor.checkJobExpire(jobInfo);
        });
    }
}
