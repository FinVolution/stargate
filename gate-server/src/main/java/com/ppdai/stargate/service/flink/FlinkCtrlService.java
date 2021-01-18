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
        return flinkCtrlClient.createCluster(baseUrl, sessionClusterArgs);
    }

    /**
     * 启动job
     *
     * @param baseUrl http://localhost:8009
     * @return
     */
    public String start(String baseUrl, FlinkJobArgs flinkJobArgs, String env) {
        return flinkCtrlClient.start(baseUrl, flinkJobArgs);
    }


    /**
     * 获取job详情
     *
     * @param baseUrl
     * @param jobId
     * @return
     */
    public JobInfoResp getJobDetails(String baseUrl, String jobId, String env, BaseFlinkReq baseFlinkReq) {
        return flinkCtrlClient.getJobDetails(baseUrl, jobId, baseFlinkReq);
    }

    public Map<String, String> getJobBaseInfo(String baseUrl, String env, BaseFlinkReq baseFlinkReq) {
        return flinkCtrlClient.getJobBaseInfo(baseUrl, baseFlinkReq);
    }

    private boolean isPro(String env) {
        return "pro".equalsIgnoreCase(env) || "pre".equalsIgnoreCase(env);
    }

    public String cancelJob(String baseUrl, String env, SavepointTriggerReq savepointTrigger) {
        return flinkCtrlClient.cancelJob(baseUrl, savepointTrigger);
    }
}
