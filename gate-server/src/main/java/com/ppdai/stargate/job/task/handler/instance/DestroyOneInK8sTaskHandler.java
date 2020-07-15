package com.ppdai.stargate.job.task.handler.instance;

import com.ppdai.stargate.constant.InstanceStatusEnum;
import com.ppdai.stargate.constant.JobTaskTypeEnum;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.AbstractTaskHandler;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.*;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class DestroyOneInK8sTaskHandler extends AbstractTaskHandler {
    @Autowired
    private InstanceService instanceService;

    @Autowired
    private ContainerService containerService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ResourceService resourceService;

    @Override
    public String getName() {
        return JobTaskTypeEnum.DESTROY_ONE.name();
    }

    @Override
    public void execute(TaskInfo taskInfo) throws Exception {
        JobInfo jobInfo = taskInfo.getJobInfo();
        log.info("开始向k8s销毁容器: taskId={}, jobId={}, instanceId={}",
                taskInfo.getId(), jobInfo.getId(), taskInfo.getInstanceId());

        InstanceEntity instanceEntity = getInstance(taskInfo);

        boolean shouldDelete = true;

        try {
            shouldDelete = Boolean.valueOf(taskInfo.getDataMap().get("delete").toString());
        } catch (Exception ex) {}

        BaseException stopException = null;
        boolean forceDelete = false;

        // 向云平台停止容器
        containerService.stopContainer(instanceEntity);

        // 等待云平台停止结束
        try {
            containerService.waitUntilDeleteOrTimeout(instanceEntity, taskInfo.getExpireTime());
        } catch (BaseException ex) {
            log.error(ex.getMessage());
            forceDelete = true;
            stopException = ex;
        }

        /**
         * 如果无法删除则表示物理机宕机，需要强制删除
         */
        if (forceDelete) {
            V1PodList v1PodList = containerService.getContainersByInstanceName(instanceEntity.getEnv(),
                    instanceEntity.getNamespace(), instanceEntity.getName(), instanceEntity.getZone());

            Optional<V1Pod> podOpt = v1PodList.getItems()
                    .stream()
                    .filter(x -> x.getMetadata().getName().equals(instanceEntity.getName()))
                    .findFirst();

            if (podOpt.isPresent()) {
                V1Pod v1Pod = podOpt.get();
                V1Node v1Node =  nodeService.getNodeByIp(instanceEntity.getEnv(), instanceEntity.getZone(), v1Pod.getStatus().getHostIP());
                if (v1Node == null) {
                    throw BaseException.newException(MessageType.ERROR, "无法获取node: nodeIP=%s",
                            v1Pod.getStatus().getHostIP());
                }

                if (nodeService.isNodeDown(v1Node)) {
                    log.info("node is down, force stop, nodeIp={}, instance={}", v1Pod.getStatus().getHostIP(), instanceEntity.getName());

                    containerService.forceStopContainer(instanceEntity);

                    // 等待云平台部署结束
                    containerService.waitUntilDeleteOrTimeout(instanceEntity, taskInfo.getExpireTime());
                } else {
                    // docker或k8s内部异常导致无法删除抛异常出来
                    throw stopException;
                }
            }
        }

        log.info("实例销毁成功: instance={}", instanceEntity.getName());

        instanceEntity.setStatus(InstanceStatusEnum.STOP_CONTAINER_IN_K8S.name());

        if (shouldDelete) {
            // 释放被实例占用的资源
            resourceService.removePodNameFromResource(instanceEntity.getName());

            log.info("资源释放成功: instance={}", instanceEntity.getName());

            instanceEntity.setIsActive(false);
        }

        instanceService.saveInstance(instanceEntity);

        log.info("保存实例到数据库成功: instance={}", instanceEntity.getName());
    }
}
