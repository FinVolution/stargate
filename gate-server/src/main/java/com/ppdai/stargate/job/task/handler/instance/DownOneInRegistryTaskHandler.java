package com.ppdai.stargate.job.task.handler.instance;

import com.ppdai.stargate.constant.JobTaskTypeEnum;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.AbstractTaskHandler;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DownOneInRegistryTaskHandler extends AbstractTaskHandler {

    @Autowired
    private TrafficService trafficService;

    @Autowired
    private AppService appService;

    @Override
    public String getName() {
        return JobTaskTypeEnum.DOWN_ONE.name();
    }

    @Override
    public void execute(TaskInfo taskInfo) {
        JobInfo jobInfo = taskInfo.getJobInfo();

        log.info("开始下线实例: instanceId={}, jobId={}, taskId={}",
                taskInfo.getInstanceId(), jobInfo.getId(), taskInfo.getId());

        InstanceEntity instanceEntity = getInstance(taskInfo);

        String domain = appService.getAppDomainByEnv(instanceEntity.getAppId(), instanceEntity.getEnv());

        trafficService.pullOutOne(domain, instanceEntity);
        log.info("实例拉出流量成功, instance={}", instanceEntity.getName());

        log.info("下线实例成功: instance={}, jobId={}, taskId={}",
                instanceEntity.getName(), jobInfo.getId(), taskInfo.getId());
    }
}
