package com.ppdai.stargate.job;

import com.ppdai.stargate.constant.JobTypeEnum;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.dao.JobRepository;
import com.ppdai.stargate.exception.BaseException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Component
@Slf4j
@ConfigurationProperties(prefix = "stargate.job.executor")
public class JobExecutor {

    private int expireTime = Integer.MAX_VALUE;
    private Map<Long, JobInfo> runningJobs = new ConcurrentHashMap<>();
    @Autowired
    private HandlerRegistry<JobHandler> jobHandlerRegistry;
    @Resource(name = "job.threadPool")
    private ThreadPoolTaskExecutor threadPool;
    @Resource(name = "job.futureThreadPool")
    private ThreadPoolTaskExecutor futureThreadPool;
    @Autowired
    private JobRepository jobRepository;

    public void submitJob(JobInfo jobInfo) {
        // currently only default job type is supported and in-use
        Optional<JobHandler> optional = jobHandlerRegistry.getHandler(JobTypeEnum.defaultType.name());
        if (!optional.isPresent()) {
            log.error("无法获取job处理器: name={}, id={}", jobInfo.getId(), jobInfo.getName());
            throw BaseException.newException(MessageType.ERROR, "无法获取job处理器: name=%s, id=%s", jobInfo.getId(), jobInfo.getName());
        }
        JobHandler jobHandler = optional.get();

        runningJobs.put(jobInfo.getId(), jobInfo);
        Future<JobInfo> future = threadPool.submit(() -> {
            MDC.put("Group", jobInfo.getName());
            MDC.put("GroupJob", jobInfo.getName() + "/" + String.valueOf(jobInfo.getId()));

            try {
                jobInfo.setThreadId(Thread.currentThread().getId());
                jobHandler.execute(jobInfo);

                jobHandler.onSuccess(jobInfo);
                log.info("job执行成功: jobId={}", jobInfo.getId());

            } catch (Exception ex) {
                log.error("处理job发生异常, err=" + ex.getMessage(), ex);
                throw ex;
            } finally {
                MDC.remove("Group");
                MDC.remove("GroupJob");
            }

            return jobInfo;
        });
        futureThreadPool.submit(() -> {
            MDC.put("Group", jobInfo.getName());
            MDC.put("GroupJob", jobInfo.getName() + "/" + String.valueOf(jobInfo.getId()));

            int expireTime = jobInfo.getExpireTime() > 0 ? jobInfo.getExpireTime() : this.expireTime;
            try {
                future.get(expireTime, TimeUnit.SECONDS);
                log.info("job在预定时间内执行完成: jobId={}", jobInfo.getId());
            } catch (TimeoutException e) {
                runningJobs.remove(jobInfo.getId());
                log.error("job执行失败!!!, 超时: jobId=" + jobInfo.getId(), e);
                jobHandler.onExpire(jobInfo);
            } catch (InterruptedException e) {
                runningJobs.remove(jobInfo.getId());
                log.error("job执行失败!!!, 被中断: jobId=" + jobInfo.getId(), e);
                jobHandler.onInterrupt(jobInfo);
            } catch (Exception e) {
                runningJobs.remove(jobInfo.getId());
                log.error("job执行失败!!!, 发生异常: jobId=" + jobInfo.getId(), e);
                if (e.getCause() != null && e.getCause().getCause() instanceof BaseException) {
                    BaseException baseException = (BaseException) e.getCause().getCause();
                    jobInfo.setAdditionalInfo(baseException.getMessage());
                    log.error("jobId=" + jobInfo.getId() + " 异常信息: " + baseException.getMessage(), e);
                }
                jobHandler.onFail(jobInfo);
            } finally {
                MDC.remove("Group");
                MDC.remove("GroupJob");
            }
        });
    }

    public void checkJobExpire(JobInfo jobInfo) {
        // currently only default job type is supported and in-use
        Optional<JobHandler> optional = jobHandlerRegistry.getHandler(JobTypeEnum.defaultType.name());
        if (!optional.isPresent()) {
            throw BaseException.newException(MessageType.ERROR, "can not find job handler by name=%s, id=%s", jobInfo.getId(), jobInfo.getName());
        }
        JobHandler jobHandler = optional.get();

        // 查看job的运行时间是否已经超时
        Date lastUpdateTime = jobInfo.getUpdateTime();
        // 放宽过期时间至3倍
        Integer expireTime = jobInfo.getExpireTime() * 3;
        if (lastUpdateTime != null && expireTime > 0) {
            Date now = jobRepository.findCurrentTime();
            Long timeInSeconds = (now.getTime() - lastUpdateTime.getTime()) / 1000L;
            if (timeInSeconds > expireTime) {
                log.info("The job has been running out of time: id=" + jobInfo.getId());
                jobHandler.onExpire(jobInfo);
            }
        }

    }

    public boolean isFullLoad() {
        return threadPool.getThreadPoolExecutor().getQueue().size() > 0;
    }


    @PreDestroy
    public void beforeDestory() {
        threadPool.setAwaitTerminationSeconds(600);
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        threadPool.shutdown();
        futureThreadPool.setAwaitTerminationSeconds(600);
        futureThreadPool.setWaitForTasksToCompleteOnShutdown(true);
        futureThreadPool.shutdown();
    }
}
