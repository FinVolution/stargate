package com.ppdai.stargate.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OnStartConfig implements InitializingBean {

    @Value("${stargate.task.log:}")
    private String taskLog;

    @Override
    public void afterPropertiesSet() {
        System.setProperty("tasklog", taskLog);
    }
}
