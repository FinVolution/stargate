package com.ppdai.stargate.job.task.handler.flink;

import com.alibaba.fastjson.JSONObject;
import com.ppdai.stargate.constant.JobTaskTypeEnum;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.AbstractTaskHandler;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.po.GroupEntity;
import com.ppdai.stargate.po.HadoopConfigEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.remote.RemoteManager;
import com.ppdai.stargate.service.GroupService;
import com.ppdai.stargate.service.HadoopConfigService;
import com.ppdai.stargate.service.InstanceService;
import com.ppdai.stargate.service.flink.FlinkCtrlService;
import com.ppdai.stargate.service.flink.PhoenixClientService;
import com.ppdai.stargate.service.flink.model.SavepointTriggerReq;
import com.ppdai.stargate.utils.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.ppdai.stargate.utils.Strings.toStringIfNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Slf4j
public class StopFlinkJobTaskHandler extends AbstractTaskHandler {

    @Autowired
    private GroupService groupService;
    @Autowired
    private FlinkCtrlService flinkCtrlService;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private RemoteManager remoteManager;

    @Autowired
    private HadoopConfigService hadoopConfigService;
    @Autowired
    private PhoenixClientService phoenixClientService;

    @Override
    public String getName() {
        return JobTaskTypeEnum.STOP_FLINKJOB.name();
    }

    @Override
    public void execute(TaskInfo taskInfo) throws Exception {
        JobInfo jobInfo = taskInfo.getJobInfo();
        log.info("【flink job：stop start】开始停止FlinkJob: taskId={}, jobId={}, instanceId={}",
                taskInfo.getId(), jobInfo.getId(), taskInfo.getInstanceId());

        InstanceEntity instanceEntity = getInstance(taskInfo);

        Long groupId = jobInfo.getGroupId();
        GroupEntity group = groupService.getGroupById(groupId);
        if (group == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定的发布组不存在，groupId = " + groupId);
            log.error("<<StopFlinkJobTaskHandler>> " + ex.getMessage());
            throw ex;
        }
        SavepointTriggerReq savepointTrigger = buildSavepoint(instanceEntity);
        log.info("【flink job：stop start02】开始停止FlinkJob: savepoint={}", savepointTrigger);
        String url = phoenixClientService.phoenixAddress(instanceEntity.getEnv(), instanceEntity.getZone());
        String savepointPath = flinkCtrlService.cancelJob(url, instanceEntity.getEnv(), savepointTrigger);
        String envVars = instanceEntity.getEnvVars();
        JSONObject jsonObject = JSONObject.parseObject(envVars);
        if (isNotBlank(savepointPath) && !"Acknowledge".equalsIgnoreCase(savepointPath)) {
            jsonObject.put("savepointPath", savepointPath);
            instanceEntity.setEnvVars(jsonObject.toJSONString());
            instanceService.saveInstance(instanceEntity);
        }
        jsonObject.remove("DASHBOARD_ADDRESS");
        jsonObject.remove("POD_NAME");
        jsonObject.remove("DESTROY");
        jsonObject.put("STOP",true);
        instanceEntity.setEnvVars(jsonObject.toJSONString());
//        group.getEnvProps().remove("JOB_ID");
        log.info("【flink job：stop end】停止FlinkJob成功, jobId={},savepoint:{}", savepointTrigger.getJobId(), savepointPath);
        instanceService.saveInstance(instanceEntity);
    }

    private SavepointTriggerReq buildSavepoint(InstanceEntity instanceEntity) {
        String envVars = instanceEntity.getEnvVars();
        JSONObject jsonObject = Strings.stringToJsonObject(envVars);
        String sessionClusterId = jsonObject.getString("SESSION_CLUSTER_ID");
        String hadoopCluster = jsonObject.getString("HADOOP_CLUSTER");
        Boolean destroy = jsonObject.getBoolean("DESTROY");
        String jobIdObj = jsonObject.getString("JOB_ID");
        String k8sRpc = remoteManager.getK8sMasterRpcUrl(instanceEntity.getEnv(), instanceEntity.getZone());
        SavepointTriggerReq savepointTrigger = new SavepointTriggerReq();
        savepointTrigger.setCancelJob(true);
        savepointTrigger.setClusterId(sessionClusterId);
        savepointTrigger.setK8sMasterUrl(k8sRpc);
        savepointTrigger.setNamespace(instanceEntity.getNamespace());
        savepointTrigger.setJobId(toStringIfNotEmpty(jobIdObj, null));
        if (destroy != null && destroy) {
            savepointTrigger.setDestroy(true);
            savepointTrigger.setJobId(null);
        } else if (hadoopCluster != null) {
            HadoopConfigEntity hadoopConfigEntity = hadoopConfigService.findHadoopConfigByName(instanceEntity.getEnv(), hadoopCluster.toString());
            log.info("【flink stop hadoopConfig exist = {}】", hadoopConfigEntity != null);
            savepointTrigger.setTargetDirectory(hadoopConfigEntity == null ? null : hadoopConfigEntity.getSavepoint());
        } else {
            Object savepoint = Strings.stringToJsonObject(instanceEntity.getEnvVars()).get("savepoint");
            savepointTrigger.setTargetDirectory(toStringIfNotEmpty(savepoint, null));
        }
        return savepointTrigger;
    }
}
