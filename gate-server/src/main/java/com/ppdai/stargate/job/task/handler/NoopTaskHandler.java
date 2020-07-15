package com.ppdai.stargate.job.task.handler;

import com.ppdai.stargate.constant.JobTaskTypeEnum;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.AbstractTaskHandler;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.AppService;
import com.ppdai.stargate.service.GroupService;
import com.ppdai.stargate.service.InstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NoopTaskHandler extends AbstractTaskHandler {

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private AppService appService;

    @Autowired
    private Environment environment;

    @Override
    public String getName() {
        return JobTaskTypeEnum.NOOP.name();
    }

    @Override
    public void execute(TaskInfo taskInfo) {

        JobInfo jobInfo = taskInfo.getJobInfo();
        long sleep = Long.parseLong(environment.getProperty("stargate.podHa.waitSecsAfterPulloutDefault", "1"));

        ApplicationEntity applicationEntity = null;
        InstanceEntity instanceEntity = instanceService.findById(taskInfo.getInstanceId());
        if (instanceEntity == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定的实例不存在, instanceName = %s", jobInfo.getName());
            log.error("<<NoopTaskHandler>> " + ex.getMessage());
            return;
        }

        applicationEntity = appService.getAppByCmdbId(instanceEntity.getAppId());

        String sleepConfigs = environment.getProperty("stargate.podHa.waitSecsAfterPulloutApps", "");
        for (String row : sleepConfigs.split(",")) {
            String[] fields = row.split(":");
            if (fields.length > 1) {
                if (applicationEntity.getName().equalsIgnoreCase(fields[0].trim())) {
                    try {
                        sleep = Integer.parseInt(fields[1]);
                    } catch (Exception ex) {}
                }
            }
        }

        log.info("begin sleep {} seconds", sleep);

        try {
            Thread.sleep(sleep * 1000);
        } catch (Exception ex) {}

        log.info("end sleep {} seconds", sleep);
    }
}
