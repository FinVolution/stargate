package com.ppdai.stargate.job.task.handler.instance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ppdai.stargate.constant.InstanceStatusEnum;
import com.ppdai.stargate.constant.JobTaskTypeEnum;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.dto.DnsDto;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.AbstractTaskHandler;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.*;
import com.ppdai.stargate.vo.EnvVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

@Component
@Slf4j
public class DeployOneInK8sTaskHandler extends AbstractTaskHandler {
    @Autowired
    private InstanceService instanceService;

    @Autowired
    private ContainerService containerService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private DnsService dnsService;

    @Override
    public String getName() {
        return JobTaskTypeEnum.DEPLOY_ONE.name();
    }

    @Override
    public void execute(TaskInfo taskInfo) throws Exception {
        JobInfo jobInfo = taskInfo.getJobInfo();
        log.info("开始向k8s部署容器: taskId={}, jobId={}, instanceId={}",
                taskInfo.getId(), jobInfo.getId(), taskInfo.getInstanceId());

        InstanceEntity instanceEntity = getInstance(taskInfo);

        String appId = instanceEntity.getAppId();
        ApplicationEntity applicationEntity = getApplication(appId);

        EnvVO envVO = getEnv(instanceEntity.getEnv());

        // 设置实例资源超售
        instanceEntity = instanceService.setResourceOverSubscribeFlag(instanceEntity);

        // 若实例zone为空，则根据数据库中的zone分配情况为其分配最优zone，保证每个zone的实例数趋于相等
        if (StringUtils.isEmpty(instanceEntity.getZone())) {
            instanceEntity = instanceService.allotZoneForInstance(instanceEntity);
            log.info("为实例分配zone成功: instance={}, zone={}", instanceEntity.getName(), instanceEntity.getZone());
        }

        String resourceIp;
        // 为实例分配资源
        if (StringUtils.isNotEmpty(instanceEntity.getSlotIp())) {
            resourceIp = resourceService.setPodNameForResource(appId,
                    instanceEntity.getEnv(),
                    instanceEntity.getSpec(),
                    instanceEntity.getSlotIp(),
                    instanceEntity.getZone(),
                    instanceEntity.getName());
        } else {
            resourceIp = resourceService.allocateResourceForPodName(appId,
                    applicationEntity.getName(),
                    instanceEntity.getEnv(),
                    instanceEntity.getSpec(),
                    instanceEntity.getName(),
                    instanceEntity.getZone());
            instanceEntity.setSlotIp(resourceIp);
            instanceService.saveInstance(instanceEntity);
        }

        log.info("为实例分配IP成功: instance={}, ip={}", instanceEntity.getName(), resourceIp);

        // 创建namespace
        containerService.createNamespace(instanceEntity.getEnv(), instanceEntity.getZone(), instanceEntity.getNamespace());

        // 创建service, 为了高可用使用
        containerService.createService(instanceEntity.getEnv(), instanceEntity.getZone(), applicationEntity.getName(), instanceEntity.getNamespace());

        log.info("创建Service成功: Service={}", instanceEntity.getName());

        // 向云平台启动容器
        containerService.startContainer(envVO.getDockeryard(), envVO.getDns(), instanceEntity);

        // 等待云平台部署结束
        containerService.waitUntilSuccOrTimeout(instanceEntity, taskInfo.getExpireTime());

        log.info("实例部署成功: instance={}", instanceEntity.getName());

        DnsDto dnsDto = new DnsDto();
        dnsDto.setName(getAppDomain(applicationEntity, instanceEntity.getEnv()));
        dnsDto.setEnvId(instanceEntity.getEnv());
        dnsDto.setContent(envVO.getNginx());
        dnsDto.setType("A");
        dnsService.add(Arrays.asList(dnsDto));

        log.info("添加DNS记录成功: domain={}, ip={}", dnsDto.getName(), dnsDto.getContent());

        instanceEntity.setStatus(InstanceStatusEnum.START_CONTAINER_IN_K8S.name());
        instanceEntity.setReleaseTime(new Date());
        instanceService.saveInstance(instanceEntity);

        log.info("保存实例到数据库成功: instance={}", instanceEntity.getName());
    }
}
