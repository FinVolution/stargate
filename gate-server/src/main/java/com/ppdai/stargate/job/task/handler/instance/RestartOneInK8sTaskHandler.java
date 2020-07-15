package com.ppdai.stargate.job.task.handler.instance;

import com.alibaba.fastjson.JSONObject;
import com.ppdai.stargate.client.JsonHttpClient;
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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class RestartOneInK8sTaskHandler extends AbstractTaskHandler {
    @Autowired
    private InstanceService instanceService;

    @Autowired
    private ContainerService containerService;

    @Autowired
    private JsonHttpClient dockerHttpClient;

    @Autowired
    private Environment environment;

    private static String RESTART_SUFFIX = "-restart";

    private void restart(TaskInfo taskInfo, InstanceEntity instanceEntity, ApplicationEntity applicationEntity, EnvVO envVO) throws IOException, ApiException {
        // 获取实例镜像
        String image = instanceEntity.getImage();
        String[] splits = image.split(":");
        String imageRepo = splits[0];
        String imageTag = splits[1];

        /**
         * 修改镜像tag
         * docker.io/demo:xx ---> docker.io/demo:xx-restart
         * docker.io/demo:xx-restart ---> docker.io/demo:xx
         */
        String newTag;
        if (imageTag.endsWith(RESTART_SUFFIX)) {
            newTag = imageTag.substring(0, imageTag.length() - RESTART_SUFFIX.length());
        } else {
            newTag = imageTag + RESTART_SUFFIX;
        }

        // 查询镜像tag列表
        String queryUrl = "http://" + envVO.getDockeryard() + "/v2/" + imageRepo + "/tags/list";
        String queryResponse = dockerHttpClient.get(queryUrl);
        JSONObject jsonObject = JSONObject.parseObject(queryResponse);
        List<String> tagList = (List<String>) jsonObject.get("tags");

        // 若新tag不存在则进行上传
        boolean containNewTag = tagList.contains(newTag);
        if (!containNewTag) {
            try {
                // 获取原镜像的manifest
                Map<String, String> getHeaders = new HashMap<>();
                getHeaders.put("accept", "application/vnd.docker.distribution.manifest.v2+json");
                String getUrl = "http://" + envVO.getDockeryard() + "/v2/" + imageRepo + "/manifests/" + imageTag;
                String manifest = dockerHttpClient.get(getUrl, getHeaders);

                // 上传新tag
                String token = environment.getProperty("stargate.job.token", "");
                Map<String, String> putHeaders = new HashMap<>();
                putHeaders.put("Content-Type", "application/vnd.docker.distribution.manifest.v2+json");
                putHeaders.put("Authorization", "Bearer " + token);
                String putUrl = "http://" + envVO.getDockeryard() + "/v2/" + imageRepo + "/manifests/" + newTag;
                dockerHttpClient.put(putUrl, putHeaders, manifest);
            } catch (IOException ex) {
                if (!ex.getMessage().contains("has already been pushed")) {
                    log.error("上传镜像tag失败, err=" + ex.getMessage(), ex);
                    throw ex;
                }
            }
        }

        // 修改实例镜像
        instanceEntity.setImage(imageRepo + ":" + newTag);

        // 向云平台更新容器
        containerService.updateContainer(envVO.getDockeryard(), instanceEntity);

        // 等待云平台部署结束
        containerService.waitUntilSuccOrTimeout(instanceEntity, taskInfo.getExpireTime());

        log.info("实例重启成功: instance={}", instanceEntity.getName());

        instanceEntity.setStatus(InstanceStatusEnum.RESTART_CONTAINER_IN_K8S.name());
        instanceEntity.setReleaseTime(new Date());
        instanceService.saveInstance(instanceEntity);

        log.info("保存实例到数据库成功: instance={}", instanceEntity.getName());
    }

    @Override
    public String getName() {
        return JobTaskTypeEnum.RESTART_ONE.name();
    }

    @Override
    public void execute(TaskInfo taskInfo) throws Exception {
        JobInfo jobInfo = taskInfo.getJobInfo();
        log.info("开始向k8s重启容器: taskId={}, jobId={}, instanceId={}",
                taskInfo.getId(), jobInfo.getId(), taskInfo.getInstanceId());

        InstanceEntity instanceEntity = getInstance(taskInfo);

        String appId = instanceEntity.getAppId();
        ApplicationEntity applicationEntity = getApplication(appId);

        EnvVO envVO = getEnv(instanceEntity.getEnv());

        restart(taskInfo, instanceEntity, applicationEntity, envVO);

        log.info("发送事件成功: instance={}", instanceEntity.getName());
    }
}
