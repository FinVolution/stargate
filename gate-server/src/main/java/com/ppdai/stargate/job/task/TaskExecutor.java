package com.ppdai.stargate.job.task;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.HandlerRegistry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
@Data
@ConfigurationProperties(prefix = "stargate.task.executor")
public class TaskExecutor {

    private int expireTime = Integer.MAX_VALUE;
    @Autowired
    private HandlerRegistry<AbstractTaskHandler> taskHandlerRegistry;

    @Resource(name = "task.threadPool")
    private ThreadPoolTaskExecutor threadPool;

    public boolean executeTask(TaskInfo taskInfo) throws InterruptedException, ExecutionException, TimeoutException {
        String name = "<<" + taskInfo.getName() + ">> ";

        Optional<AbstractTaskHandler> optional = taskHandlerRegistry.getHandler(taskInfo.getName());
        if (!optional.isPresent()) {
            log.error(name + "无法获取任务处理器: id={}, name={}", taskInfo.getId(), taskInfo.getName());
            throw BaseException.newException(MessageType.ERROR, "无法获取任务处理器: id=%s, name=%s", taskInfo.getId(), taskInfo.getName());
        }

        AbstractTaskHandler taskHandler = optional.get();
        Future<TaskInfo> future = threadPool.submit(() -> {
            MDC.put("Group", taskInfo.getJobInfo().getName());
            MDC.put("GroupJob", taskInfo.getJobInfo().getName() + "/" + String.valueOf(taskInfo.getJobInfo().getId()));

            try {
                taskHandler.execute(taskInfo);
                taskHandler.onSuccess(taskInfo);

                log.info(name + "任务执行成功: taskId={}, taskName={}", taskInfo.getId(), taskInfo.getName());
            } catch (Exception ex) {
                log.error("处理任务发生异常, err=" + ex.getMessage(), ex);
                throw ex;
            } finally {
                MDC.remove("Group");
                MDC.remove("GroupJob");
            }

            return taskInfo;
        });
        int expireTime = taskInfo.getExpireTime() > 0 ? taskInfo.getExpireTime() : this.expireTime;
        try {
            future.get(expireTime, TimeUnit.SECONDS);
            return true;
        } catch (InterruptedException e) {
            log.error(name + "任务执行失败!!!, 被中断: taskId=" + taskInfo.getId() + ", taskName=" + taskInfo.getName(), e);
            taskHandler.onInterrupt(taskInfo);
            throw e;
        } catch (TimeoutException e) {
            log.error(name + "任务执行失败!!!, 超时: taskId=" + taskInfo.getId() + ", taskName=" + taskInfo.getName(), e);
            taskHandler.onExpire(taskInfo);
            throw e;
        } catch (Exception e) {
            log.error(name + "任务执行失败!!!, 发生异常: taskId=" + taskInfo.getId() + ", taskName=" + taskInfo.getName(), e);
            if (e.getCause() instanceof BaseException) {
                BaseException baseException = (BaseException) e.getCause();
                taskInfo.setAdditionalInfo(baseException.getMessage());
                log.error(name + "taskId=" + taskInfo.getId() + ", taskName=" + taskInfo.getName() + " 异常信息: " + baseException.getMessage(), e);
            }
            taskHandler.onFail(taskInfo);
            throw e;
        }
    }
}
