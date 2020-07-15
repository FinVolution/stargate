package com.ppdai.stargate.scheduler;

import com.ppdai.stargate.constant.LockEnum;
import com.ppdai.stargate.po.GlobalLockEntity;
import com.ppdai.stargate.service.DistLockService;
import com.ppdai.stargate.service.EnvService;
import com.ppdai.stargate.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@ConditionalOnProperty(name = "stargate.syncenv.enable", havingValue = "true")
@Component
@Slf4j
public class SyncEnvScheduler {

    @Value("${stargate.syncenv.fixedRate}")
    private long interval;

    @Autowired
    private EnvService envService;

    @Autowired
    private DistLockService distLockService;

    private String node = IPUtil.getLocalIP();

    @Scheduled(initialDelayString = "${stargate.syncenv.initialDelay}", fixedRateString = "${stargate.syncenv.fixedRate}")
    public void syncApp() {
        try {
            GlobalLockEntity globalLockEntity = distLockService.tryLock(LockEnum.SYNC_ENV, node, interval * 3);

            if (globalLockEntity != null
                    && distLockService.lock(LockEnum.SYNC_ENV, node, globalLockEntity.getOwner(), interval * 3)) {
                log.info("开始从atlas同步环境");
                envService.syncEnvs();
                log.info("从atlas同步环境结束");
            }
        } catch (Exception ex) {
            log.error("从atlas同步环境失败: err=" + ex.getMessage(), ex);
        }
    }
}
