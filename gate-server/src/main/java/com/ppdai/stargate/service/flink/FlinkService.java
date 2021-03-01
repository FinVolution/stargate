package com.ppdai.stargate.service.flink;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ppdai.stargate.client.K8sClient;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.remote.RemoteManager;
import com.ppdai.stargate.service.GroupService;
import com.ppdai.stargate.service.InstanceService;
import com.ppdai.stargate.service.flink.model.BaseFlinkReq;
import com.ppdai.stargate.service.flink.model.FlinkJobStatus;
import com.ppdai.stargate.service.flink.model.JobInfoResp;
import com.ppdai.stargate.service.flink.model.SessionClusterArgs;
import com.ppdai.stargate.utils.Strings;
import com.ppdai.stargate.vi.FlinkJobVO;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FlinkService {

    @Autowired
    private GroupService groupService;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private FlinkCtrlService flinkCtrlService;

    @Autowired
    private RemoteManager remoteManager;

    @Autowired
    private PhoenixClientService phoenixClientService;

    /**
     * 控制服务地址
     */
    @Value("${phoenix.url:http://10.11.127:8081}")
    private String phoenixUrl;

    protected static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5, 5,
            5, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadFactoryBuilder().setNameFormat("flink-log-pool-%d").build(),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    public FlinkJobVO getFlinkJobStatusByGroupId(Long groupId) {
        FlinkJobVO flinkJobVO = new FlinkJobVO();
        flinkJobVO.setId(1);

        List<InstanceEntity> instanceEntityList = instanceService.getInstancesByGroupId(groupId);
        if (CollectionUtils.isEmpty(instanceEntityList)) {
            throw BaseException.newException(MessageType.ERROR, "指定的发布组不存在，groupId = " + groupId);
        }
        InstanceEntity crtlInstance = instanceEntityList.get(0);
        String envVars = crtlInstance.getEnvVars();
        JSONObject jsonObject = JSONObject.parseObject(envVars);
        String savepointPath = jsonObject.getString("savepointPath");
        String sessionClusterId = jsonObject.getString("SESSION_CLUSTER_ID");
        String jobId = jsonObject.getString("JOB_ID");
        String stop = jsonObject.getString("STOP");
        String dashboardAddress = jsonObject.getString("DASHBOARD_ADDRESS");
        String hadoopCluster = jsonObject.getString("HADOOP_CLUSTER");
        String cmd = jsonObject.getString("cmd");
        Integer instanceCount = jsonObject.getInteger("INSTANCE_COUNT");
        flinkJobVO.setTaskTotal(instanceCount);
        flinkJobVO.setVersion(crtlInstance.getImage());
        flinkJobVO.setInstanceName(crtlInstance.getName());
        flinkJobVO.setSavepointPath(savepointPath);
        flinkJobVO.setCmd(cmd);
        flinkJobVO.setVariable(Strings.stringToJsonObject(crtlInstance.getEnvVars()).getString("variable"));
        flinkJobVO.setHadoopName(Strings.toStringIfNotEmpty(hadoopCluster, null));
        if (Objects.nonNull(dashboardAddress)) {
            flinkJobVO.setDashboardAddress(dashboardAddress.toString());
        }
        if (sessionClusterId != null && stop == null) {
            try {
                String k8sRpc = remoteManager.getK8sMasterRpcUrl(crtlInstance.getEnv(), crtlInstance.getZone());
                BaseFlinkReq baseFlinkReq = new BaseFlinkReq();
                baseFlinkReq.setClusterId(sessionClusterId.toString());
                baseFlinkReq.setK8sMasterUrl(k8sRpc);
                baseFlinkReq.setNamespace(crtlInstance.getNamespace());
                JobInfoResp jobInfoResp = flinkCtrlService.getJobDetails(phoenixUrl,
                        jobId.toString(), crtlInstance.getEnv(), baseFlinkReq);
                if (jobInfoResp != null) {
                    flinkJobVO.setStatus(jobInfoResp.getStatus().equals(FlinkJobStatus.RUNNING) ? "RUNNING" : "NOTRUNNING");
                    flinkJobVO.setName(jobInfoResp.getJobName());
                    flinkJobVO.setRunningTaskTotal(jobInfoResp.getRunningTaskTotal());
                }
            } catch (Exception e) {
                if ("Not Found".equalsIgnoreCase(e.getMessage()) ||
                        "java.net.UnknownHostException: unknown".equalsIgnoreCase(e.getMessage())) {

                    jsonObject.put("JOB_ID", null);
                    jsonObject.put("RESTART", true);
                    crtlInstance.setEnvVars(jsonObject.toJSONString());
                    instanceService.saveInstance(crtlInstance);
                    return flinkJobVO;
                }
                log.warn("获取clusterId{}的job详情失败：", sessionClusterId, e);
            }
        }
        Boolean restart = jsonObject.getBoolean("RESTART");
        if (Objects.nonNull(restart) && restart) {
            try {
                getFlinkJobIdAndUpdate(crtlInstance);
            } catch (Exception e) {
            }
        }
        return flinkJobVO;
    }

    public void getFlinkJobIdAndUpdate(InstanceEntity instanceEntity) {
        String envVars = instanceEntity.getEnvVars();
        JSONObject jsonObject = JSONObject.parseObject(envVars);
        String sessionClusterId = jsonObject.getString("SESSION_CLUSTER_ID");
        String k8sRpc = remoteManager.getK8sMasterRpcUrl(instanceEntity.getEnv(), instanceEntity.getZone());
        log.info("【flink job：wait start】开始获取flink job的jobid：clusterId={},namespace={},env={},k8sRpc={}",
                sessionClusterId, instanceEntity.getNamespace(), instanceEntity.getEnv(), k8sRpc);
        //获取jobid
        String url = phoenixClientService.phoenixAddress(instanceEntity.getEnv(), instanceEntity.getZone());
        Map<String, String> jobInfo;
        try {
            Map<String, String> labels = new HashMap<>();
            labels.put("instance", instanceEntity.getName());

            BaseFlinkReq baseFlinkReq = new BaseFlinkReq();
            baseFlinkReq.setClusterId(sessionClusterId);
            baseFlinkReq.setK8sMasterUrl(k8sRpc);
            baseFlinkReq.setLabels(labels);
            baseFlinkReq.setNamespace(instanceEntity.getNamespace());
            jobInfo = flinkCtrlService.getJobBaseInfo(url, instanceEntity.getEnv(), baseFlinkReq);
            jsonObject.put("RESTART", false);
        } catch (Exception e) {
            log.error("【flink job：wait error】", e);
            throw e;
        }

        jsonObject.put("JOB_ID", jobInfo.get("jobId"));
        jsonObject.put("DASHBOARD_ADDRESS", jobInfo.get("dashboard"));
        jsonObject.remove("STOP");
        instanceEntity.setEnvVars(jsonObject.toJSONString());
        instanceService.saveInstance(instanceEntity);
        String dashboard = jobInfo.get("dashboard");

        instanceEntity.setSlotIp(subIp(dashboard));
        instanceEntity.setPort(subPort(dashboard));
        instanceService.saveInstance(instanceEntity);
        log.info("【flink job：wait end】获取flink job：clusterId={},namespace={},env={},k8sRpc={}的jobid={}成功,dashboard={}",
                sessionClusterId, instanceEntity.getNamespace(), instanceEntity.getEnv(), k8sRpc,
                jobInfo.get("jobId"), jobInfo.get("dashboard"));
    }

    private String subIp(String dashboard) {
        if (StringUtils.isNotBlank(dashboard)) {
            return StringUtils.substringBetween(dashboard, "http://", ":");
        }
        return null;
    }

    private Integer subPort(String dashboard) {
        if (StringUtils.isNotBlank(dashboard)) {
            return Integer.valueOf(StringUtils.substringAfterLast(dashboard, ":"));
        }
        return null;
    }

    public void getLogAndWriteAsync(SessionClusterArgs sessionClusterArgs, TaskInfo taskInfo) {
        try {
            executor.submit(() -> {
                MDC.put("Group", taskInfo.getJobInfo().getName());
                MDC.put("GroupJob", taskInfo.getJobInfo().getName() + "/" + String.valueOf(taskInfo.getJobInfo().getId()));
                ApiClient client = new ApiClient();
                Configuration.setDefaultApiClient(client);
                client.setBasePath(sessionClusterArgs.getK8sMasterUrl());
                CoreV1Api coreApi = new CoreV1Api(client);
                String labelSelector = "app" + "=" + sessionClusterArgs.getClusterId() + ",component=jobmanager";
                PodLogs logs = new PodLogs();
                V1Pod pod = null;
                try {
                    Thread.sleep(4_000);
                    pod = coreApi.listNamespacedPod(sessionClusterArgs.getNamespace(), null, "true",
                            null, null, labelSelector, 50, null, 30, null)
                            .getItems()
                            .get(0);
                } catch (Exception e) {
                }
                StringBuilder sb = new StringBuilder();
                try (InputStream is = logs.streamNamespacedPodLog(pod)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    while (reader.read() != -1) {
                        String line = reader.readLine();
                        sb.append(line).append("\n\t");
                    }
                    log.info(sb.toString());
                } catch (Exception e) {
                    log.info(sb.toString());
                } finally {
                    MDC.remove("Group");
                    MDC.remove("GroupJob");
                }
            });
        } catch (Throwable e) {
            log.error("get job logs error", e);
        }


    }

    public String getContainerLog(InstanceEntity instanceEntity) {
        String envVars = instanceEntity.getEnvVars();
        JSONObject jsonObject = JSONObject.parseObject(envVars);

        if (jsonObject.get("POD_NAME") != null) {
            try {
                return containerLog(instanceEntity, jsonObject.getString("POD_NAME"));
            } catch (Exception e) {
                jsonObject.remove("POD_NAME");
            }
        }
        String sessionClusterId = jsonObject.getString("SESSION_CLUSTER_ID");
        String k8sRpc = remoteManager.getK8sMasterRpcUrl(instanceEntity.getEnv(), instanceEntity.getZone());
        String url = phoenixClientService.phoenixAddress(instanceEntity.getEnv(), instanceEntity.getZone());
        Map<String, String> jobInfo = new HashMap<>();
        try {
            Map<String, String> labels = new HashMap<>();
            labels.put("instance", instanceEntity.getName());
            BaseFlinkReq baseFlinkReq = new BaseFlinkReq();
            baseFlinkReq.setClusterId(sessionClusterId);
            baseFlinkReq.setK8sMasterUrl(k8sRpc);
            baseFlinkReq.setLabels(labels);
            baseFlinkReq.setNamespace(instanceEntity.getNamespace());
            jobInfo = flinkCtrlService.getJobBaseInfo(url, instanceEntity.getEnv(), baseFlinkReq);
        } catch (Exception e) {
            log.error("【flink job：wait error】", e);
        }
        if (jobInfo.get("podName") != null) {
            jsonObject.put("POD_NAME", jobInfo.get("podName"));
            instanceEntity.setEnvVars(jsonObject.toJSONString());
            instanceService.saveInstance(instanceEntity);
            return containerLog(instanceEntity, jobInfo.get("podName"));
        }
        return StringUtils.EMPTY;
    }

    private String containerLog(InstanceEntity instanceEntity, String name) {
        try {
            K8sClient k8sClient = remoteManager.getK8sClientByZone(instanceEntity.getEnv(), instanceEntity.getZone());
            return k8sClient.getAppPodLog(instanceEntity.getNamespace(), name);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw BaseException.newException(MessageType.ERROR, "查询容器日志失败, err=" + ex.getMessage());
        }
    }


}
