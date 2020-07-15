package com.ppdai.stargate.scheduler;

import com.ppdai.stargate.job.JobMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@EnableScheduling
@ConditionalOnProperty(name = "stargate.scheduler.enable", havingValue = "true")
@Component
@Slf4j
public class JobSchedule {
    @Autowired
    private JobMonitor jobMonitor;

    @Scheduled(initialDelayString = "${stargate.scheduler.initialDelay}", fixedRateString = "${stargate.scheduler.fixedRate}")
    public void scanNewJob() {
        jobMonitor.processNewJob();
    }

    @Scheduled(initialDelayString = "${stargate.scheduler.initialDelay}", fixedRateString = "${stargate.scheduler.fixedRate}")
    public void scanTimeOut() {
        jobMonitor.processExpiredJob();
    }
}
