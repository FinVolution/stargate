package com.ppdai.stargate.job.task.handler.flink;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ppdai.atlas.client.invoker.ApiException;
import com.ppdai.stargate.constant.JobTaskTypeEnum;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.dao.ApplicationRepository;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.AbstractTaskHandler;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.GroupEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.remote.RemoteManager;
import com.ppdai.stargate.service.GroupService;
import com.ppdai.stargate.service.InstanceService;
import com.ppdai.stargate.service.flink.FlinkCtrlService;
import com.ppdai.stargate.service.flink.FlinkService;
import com.ppdai.stargate.service.flink.PhoenixClientService;
import com.ppdai.stargate.service.flink.model.SessionClusterArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.ppdai.stargate.constant.FlinkConfigOptions.*;
import static com.ppdai.stargate.utils.Strings.toStringIfNotEmpty;
import static java.lang.Boolean.parseBoolean;

@Component
@Slf4j
public class DeployFlinkJobInK8sTaskHandler extends AbstractTaskHandler {

    @Autowired
    private GroupService groupService;
    @Autowired
    private FlinkCtrlService flinkCtrlService;
    @Autowired
    private RemoteManager remoteManager;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private PhoenixClientService phoenixClientService;
    @Autowired
    private FlinkService flinkService;
    @Autowired
    private ApplicationRepository applicationRepository;


    @Override
    public String getName() {
        return JobTaskTypeEnum.DEPLOY_FLINKJOB.name();
    }

    @Override
    public void execute(TaskInfo taskInfo) throws ApiException {

        JobInfo jobInfo = taskInfo.getJobInfo();
        InstanceEntity instanceEntity = getInstance(taskInfo);
        log.info("【flink job：deploy start】开始向k8s部署FlinkJob: taskId={}, jobId={}, instanceId={}",
                taskInfo.getId(), jobInfo.getId(), instanceEntity.getId());
        Long groupId = jobInfo.getGroupId();
        GroupEntity group = groupService.getGroupById(groupId);
        if (group == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定的发布组不存在，groupId = " + groupId);
            log.error("<<DeployFlinkJobInK8sTaskHandler>> " + ex.getMessage());
            throw ex;
        }

        SessionClusterArgs sessionClusterArgs = applyFlinkJobArgs(jobInfo, instanceEntity);
        log.info("【flink job：deploy start02】开始向k8s部署FlinkJob: args={}", sessionClusterArgs);
        String url = phoenixClientService.phoenixAddress(instanceEntity.getEnv(), instanceEntity.getZone());
        String command = flinkCtrlService.createCluster(url,
                sessionClusterArgs, instanceEntity.getEnv());
        log.info("【flink job：deploy end】command:{}", command);
        flinkService.getLogAndWriteAsync(sessionClusterArgs, taskInfo);
        instanceEntity.setReleaseTime(new Date());
        instanceService.saveInstance(instanceEntity);
    }

    private SessionClusterArgs applyFlinkJobArgs(JobInfo jobInfo, InstanceEntity instanceEntity) {
        ApplicationEntity applicationEntity = applicationRepository.findByAppId(instanceEntity.getAppId());
        if (applicationEntity == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "应用不存在，appId = " + instanceEntity.getAppId());
            log.error("<<DeployFlinkJobInK8sTaskHandler>> " + ex.getMessage());
            throw ex;
        }
        JSONObject jsonObject = JSONObject.parseObject(instanceEntity.getEnvVars());
        Map<String, Object> dataMap = jobInfo.getDataMap();
        Object savepointSwitch = dataMap.get("savepointSwitch");
        Object savepointPathFrom = dataMap.get("savepointPath");
        HashMap<String, String> labels = Maps.newHashMap();
        labels.put("instance", instanceEntity.getName());
        labels.put("appid", instanceEntity.getAppId());
        labels.put("appname", applicationEntity.getName());
        HashMap<String, String> taskLabels = Maps.newHashMap();
        taskLabels.put("appname", applicationEntity.getName());
        taskLabels.put("appid", instanceEntity.getAppId());
        taskLabels.put("instance", instanceEntity.getName());
        String savepoint = jsonObject.getString("savepointPath");
        String sessionClusterId = jsonObject.getString("SESSION_CLUSTER_ID");
        String hadoopCluster = jsonObject.getString("HADOOP_CLUSTER");
        String cmd = jsonObject.getString("cmd");
        String savepointPath = toStringIfNotEmpty(savepointPathFrom, toStringIfNotEmpty(savepoint, null));
        SessionClusterArgs sessionClusterArgs = new SessionClusterArgs();
        sessionClusterArgs.setClusterId(sessionClusterId);
        sessionClusterArgs.setNamespace(instanceEntity.getNamespace());
        sessionClusterArgs.setConfigFile(KUBE_CONFIG.getDefaultValue());
        sessionClusterArgs.setRestServiceExposedType("NodePort");
        sessionClusterArgs.setContainerImage(jsonObject.getString("imageUrl"));
        sessionClusterArgs.setJobManagerCpu(JOBMANAGER_CPU.getDefaultValue());
        sessionClusterArgs.setTaskManagerCpu(1);
        sessionClusterArgs.setJobManagerMemoryProcessSize(JOBMANAGER_PROCESSSIZE.getDefaultValue());
        sessionClusterArgs.setTaskManagerMemorySize(1024 + "m");
        sessionClusterArgs.setParallelism(jsonObject.getInteger("parallelism"));
        sessionClusterArgs.setTaskManagerNumberOfTaskSlots(jsonObject.getInteger("taskSlots"));
        sessionClusterArgs.setVariable(jsonObject.getString("variable"));
        sessionClusterArgs.setHadoopConfigMapName(toStringIfNotEmpty(hadoopCluster, HADOOP_CONFIG_MAP.getDefaultValue()));
        boolean isSwitch = parseBoolean(toStringIfNotEmpty(savepointSwitch, "true"));
        String finalSavepointPath = isSwitch ? savepointPath : null;
        sessionClusterArgs.setSavepointDirectory(finalSavepointPath);
        sessionClusterArgs.setJobManagerLabels(labels);
        sessionClusterArgs.setTaskManagerLabels(taskLabels);
        sessionClusterArgs.setCmd(cmd);
        String k8sRpc = remoteManager.getK8sMasterRpcUrl(instanceEntity.getEnv(), instanceEntity.getZone());
        sessionClusterArgs.setK8sMasterUrl(k8sRpc);
        return sessionClusterArgs;
    }
}
