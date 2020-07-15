package com.ppdai.stargate.job;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stargate.job")
@Data
public class JobExecuteProperties {
    private ThreadPool threadPool = new ThreadPool();
    private int expireTime = Integer.MAX_VALUE;

    @Data
    public static class ThreadPool {
        private int corePoolSize = 10;
        private int maxPoolSize = 100;
        private int keepAliveSeconds = 300;
        private int queueCapacity = 100;
    }
}
