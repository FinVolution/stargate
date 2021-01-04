package com.ppdai.stargate.service.flink;

import com.ppdai.stargate.service.flink.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class FlinkCtrlService {

    private final FlinkCtrlClient flinkCtrlClient;

    public FlinkCtrlService(FlinkCtrlClient flinkCtrlClient) {
        this.flinkCtrlClient = flinkCtrlClient;
    }

    /**
     * 创建session cluster
     *
     * @param baseUrl
     * @param sessionClusterArgs
     * @return
     */
    public String createCluster(String baseUrl, SessionClusterArgs sessionClusterArgs,
                                String env) {
        if (isPro(env)) {
            return flinkCtrlClient.createCluster(baseUrl, sessionClusterArgs);
        } else {
            return flinkCtrlClient.createClusterByProxy(baseUrl, sessionClusterArgs);
        }
    }

    /**
     * 启动job
     *
     * @param baseUrl http://localhost:8009
     * @return
     */
    public String start(String baseUrl, FlinkJobArgs flinkJobArgs, String env) {
        if (isPro(env)) {
            return flinkCtrlClient.start(baseUrl, flinkJobArgs);
        } else {
            return flinkCtrlClient.startByProxy(baseUrl, flinkJobArgs);
        }
    }


    /**
     * 获取job详情
     *
     * @param baseUrl
     * @param jobId
     * @param clusterId
     * @return
     */
    public JobInfoResp getJobDetails(String baseUrl, String jobId, String env, BaseFlinkReq baseFlinkReq) {
        if (isPro(env)) {
            return flinkCtrlClient.getJobDetails(baseUrl, jobId, baseFlinkReq);
        } else {
            return flinkCtrlClient.getJobDetailsByProxy(baseUrl, jobId, baseFlinkReq);
        }
    }

    public Map<String, String> getJobBaseInfo(String baseUrl, String env, BaseFlinkReq baseFlinkReq) {
        if (isPro(env)) {
            return flinkCtrlClient.getJobBaseInfo(baseUrl, baseFlinkReq);
        } else {
            return flinkCtrlClient.getJobBaseInfoByProxy(baseUrl, baseFlinkReq);
        }
    }

    private boolean isPro(String env) {
        return "pro".equalsIgnoreCase(env) || "pre".equalsIgnoreCase(env);
    }

    public String cancelJob(String baseUrl, String env, SavepointTriggerReq savepointTrigger) {
        if (isPro(env)) {
            return flinkCtrlClient.cancelJob(baseUrl, savepointTrigger);
        } else {
            return flinkCtrlClient.cancelJobByProxy(baseUrl, savepointTrigger);
        }
    }
}
