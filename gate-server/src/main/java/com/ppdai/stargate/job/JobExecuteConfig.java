package com.ppdai.stargate.job;

import com.ppdai.stargate.job.task.AbstractTaskHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableConfigurationProperties(JobExecuteProperties.class)
public class JobExecuteConfig {
    @Autowired
    private JobExecuteProperties jobExecuteProperties;

    private ThreadPoolTaskExecutor createExecuteThreadPool() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(jobExecuteProperties.getThreadPool().getCorePoolSize());
        threadPool.setMaxPoolSize(jobExecuteProperties.getThreadPool().getMaxPoolSize());
        threadPool.setQueueCapacity(jobExecuteProperties.getThreadPool().getQueueCapacity());
        threadPool.setKeepAliveSeconds(jobExecuteProperties.getThreadPool().getKeepAliveSeconds());
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPool;
    }

    @Bean("job.threadPool")
    public ThreadPoolTaskExecutor createJobExecuteThreadPool() {
        return createExecuteThreadPool();
    }

    @Bean("job.futureThreadPool")
    public ThreadPoolTaskExecutor createTaskExecuteFutureThreadPool() {
        return createExecuteThreadPool();
    }

    @Bean("task.threadPool")
    public ThreadPoolTaskExecutor createTaskExecuteThreadPool() {
        return createExecuteThreadPool();
    }

    @Bean("jobHandlerRegistry")
    public HandlerRegistry<JobHandler> createJobHandlerRegistry(ObjectProvider<List<JobHandler>> jobHandlers) {
        return new HandlerRegistry<>(jobHandlers.getIfAvailable());
    }

    @Bean("taskHandlerRegistry")
    public HandlerRegistry<AbstractTaskHandler> createTaskHandlerRegistry(ObjectProvider<List<AbstractTaskHandler>> taskHandlers) {
        return new HandlerRegistry<>(taskHandlers.getIfAvailable());
    }
}
