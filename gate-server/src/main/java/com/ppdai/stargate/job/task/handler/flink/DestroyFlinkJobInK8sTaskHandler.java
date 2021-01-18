package com.ppdai.stargate.job.task.handler.flink;

import com.ppdai.stargate.constant.JobTaskTypeEnum;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.AbstractTaskHandler;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.InstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DestroyFlinkJobInK8sTaskHandler extends AbstractTaskHandler {

    @Autowired
    private InstanceService instanceService;
    @Value("${phoenix.url:http://10.11.127:8081}")
    private String phoenixUrl;

    @Override
    public String getName() {
        return JobTaskTypeEnum.DESTROY_FLINKJOB.name();
    }

    @Override
    public void execute(TaskInfo taskInfo) throws Exception {
        JobInfo jobInfo = taskInfo.getJobInfo();
        log.info("【flink job：destroy start】开始删除FlinkJob实例: taskId={}, jobId={}, instanceId={}",
                taskInfo.getId(), jobInfo.getId(), taskInfo.getInstanceId());

        InstanceEntity instanceEntity = getInstance(taskInfo);
        instanceEntity.setIsActive(false);
        instanceService.saveInstance(instanceEntity);
        log.info("【flink job：destroy end】删除FlinkJob实例结束: taskId={}, jobId={}, instanceId={}",
                taskInfo.getId(), jobInfo.getId(), taskInfo.getInstanceId());
    }
}
