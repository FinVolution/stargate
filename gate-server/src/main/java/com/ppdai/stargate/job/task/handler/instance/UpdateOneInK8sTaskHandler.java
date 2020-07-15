package com.ppdai.stargate.job.task.handler.instance;

import com.ppdai.stargate.constant.InstanceStatusEnum;
import com.ppdai.stargate.constant.JobTaskTypeEnum;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.AbstractTaskHandler;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.ContainerService;
import com.ppdai.stargate.service.InstanceService;
import com.ppdai.stargate.vo.EnvVO;
import io.kubernetes.client.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
@Slf4j
public class UpdateOneInK8sTaskHandler extends AbstractTaskHandler {
    @Autowired
    private InstanceService instanceService;

    @Autowired
    private ContainerService containerService;

    private void update(TaskInfo taskInfo, InstanceEntity instanceEntity, ApplicationEntity applicationEntity, EnvVO envVO) throws IOException, ApiException {
        // 向云平台更新容器
        containerService.updateContainer(envVO.getDockeryard(), instanceEntity);

        // 等待云平台部署结束
        containerService.waitUntilSuccOrTimeout(instanceEntity, taskInfo.getExpireTime());

        log.info("实例更新成功: instance={}", instanceEntity.getName());

        instanceEntity.setStatus(InstanceStatusEnum.UPDATE_CONTAINER_IN_K8S.name());
        instanceEntity.setReleaseTime(new Date());
        instanceService.saveInstance(instanceEntity);

        log.info("保存实例到数据库成功: instance={}", instanceEntity.getName());
    }

    @Override
    public String getName() {
        return JobTaskTypeEnum.UPDATE_ONE.name();
    }

    @Override
    public void execute(TaskInfo taskInfo) throws Exception {
        JobInfo jobInfo = taskInfo.getJobInfo();
        log.info("开始向k8s更新容器: taskId={}, jobId={}, instanceId={}",
                taskInfo.getId(), jobInfo.getId(), taskInfo.getInstanceId());

        String image = jobInfo.getDataMap().get("image").toString();

        InstanceEntity instanceEntity = getInstance(taskInfo);
        instanceEntity.setImage(image);

        String appId = instanceEntity.getAppId();
        ApplicationEntity applicationEntity = getApplication(appId);

        EnvVO envVO = getEnv(instanceEntity.getEnv());

        update(taskInfo, instanceEntity, applicationEntity, envVO);

        log.info("发送事件成功: instance={}", instanceEntity.getName());
    }
}
