package com.ppdai.stargate.service.flink;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ppdai.stargate.client.JsonHttpClient;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.service.flink.model.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Created by chenlang on 2020/7/17
 **/
@Slf4j
public class FlinkCtrlClient {

    private static final String MEDIA_TYPE = "application/json; charset=utf-8";

    private final JsonHttpClient flinkHttpClient;

    private OkHttpClient flinkProxyClient;

    public FlinkCtrlClient(JsonHttpClient flinkHttpClient, OkHttpClient okHttpClient) {
        this.flinkHttpClient = flinkHttpClient;
        this.flinkProxyClient = okHttpClient;
    }

    /**
     * 创建session cluster
     *
     * @param baseUrl
     * @param sessionClusterArgs
     * @return
     */
    public String createCluster(String baseUrl, SessionClusterArgs sessionClusterArgs) {
        String url = baseUrl + "/flink/application/job/create";
        try {
            String response = flinkHttpClient.post(url, sessionClusterArgs);
            return buildFlinkResponse(response);
        } catch (IOException e) {
            throw BaseException.newException(MessageType.ERROR, "创建flink session-cluster失败, err=" + e.getMessage());
        }
    }

    public String createClusterByProxy(String baseUrl, SessionClusterArgs sessionClusterArgs) {
        String url = baseUrl + "/flink/application/job/create";
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse(MEDIA_TYPE), JSONObject.toJSONString(sessionClusterArgs)))
                    .build();
            return buildProxyResponse(request);
        } catch (IOException e) {
            throw BaseException.newException(MessageType.ERROR, "创建flink session-cluster失败, err=" + e.getMessage());
        }

    }


    /**
     * 启动job
     *
     * @param baseUrl http://localhost:8009
     * @return
     */
    public String start(String baseUrl, FlinkJobArgs flinkJobArgs) {
        String url = baseUrl + "/jobs/" + flinkJobArgs.getJarId() + "/run";
        try {
            String response = flinkHttpClient.post(url, flinkJobArgs);
            return buildFlinkResponse(response);
        } catch (IOException e) {
            throw BaseException.newException(MessageType.ERROR, "启动job失败, err=" + e.getMessage());
        }
    }

    public String startByProxy(String baseUrl, FlinkJobArgs flinkJobArgs) {
        String url = baseUrl + "/jobs/" + flinkJobArgs.getJarId() + "/run";
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse(MEDIA_TYPE), JSONObject.toJSONString(flinkJobArgs)))
                    .build();
            return buildProxyResponse(request);
        } catch (IOException e) {
            throw BaseException.newException(MessageType.ERROR, "启动job失败, err=" + e.getMessage());
        }
    }


    /**
     * 获取job详情
     *
     * @param baseUrl
     * @param jobId
     * @return
     */
    public JobInfoResp getJobDetails(String baseUrl, String jobId, BaseFlinkReq baseFlinkReq) {
        String url = baseUrl + "/jobs/" + jobId + "/details";
        try {
            String response = flinkHttpClient.post(url, baseFlinkReq);
            Object jobinfo = buildFlinkResponse(response);
            return JSONObject.parseObject(jobinfo.toString(), JobInfoResp.class);
        } catch (Exception e) {
            throw BaseException.newException(MessageType.ERROR, e.getMessage());
        }
    }

    public JobInfoResp getJobDetailsByProxy(String baseUrl, String jobId, BaseFlinkReq baseFlinkReq) {
        String url = baseUrl + "/jobs/" + jobId + "/details";
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse(MEDIA_TYPE), JSONObject.toJSONString(baseFlinkReq)))
                    .build();
            Object jobinfo = buildProxyResponse(request);
            return JSONObject.parseObject(jobinfo.toString(), JobInfoResp.class);
        } catch (Exception e) {
            throw BaseException.newException(MessageType.ERROR, e.getMessage());
        }
    }

    public String cancelJob(String baseUrl, SavepointTriggerReq savepointTrigger) {
        String url = baseUrl + "/flink/application/job/cancel";
        try {
            String response = flinkHttpClient.post(url, savepointTrigger);
            return buildFlinkResponse(response);
        } catch (IOException e) {
            throw BaseException.newException(MessageType.ERROR, "取消job失败, err=" + e.getMessage());
        }
    }

    public String cancelJobByProxy(String baseUrl, SavepointTriggerReq savepointTrigger) {
        String url = baseUrl + "/flink/application/job/cancel";
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse(MEDIA_TYPE), JSONObject.toJSONString(savepointTrigger)))
                    .build();
            return buildProxyResponse(request);
        } catch (IOException e) {
            throw BaseException.newException(MessageType.ERROR, "取消job失败, err=" + e.getMessage());
        }
    }

    public Map<String, String> getJobBaseInfo(String baseUrl, BaseFlinkReq baseFlinkReq) {
        String url = baseUrl + "/flink/application/jobid";
        try {
            String response = flinkHttpClient.post(url, baseFlinkReq);
            return buildFlinkResponse(response);
        } catch (Exception e) {
            throw BaseException.newException(MessageType.ERROR, e.getMessage());
        }
    }

    public Map<String, String> getJobBaseInfoByProxy(String baseUrl, BaseFlinkReq baseFlinkReq) {
        String url = baseUrl + "/flink/application/jobid";
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse(MEDIA_TYPE), JSONObject.toJSONString(baseFlinkReq)))
                    .build();
            return buildProxyResponse(request);
        } catch (Exception e) {
            throw BaseException.newException(MessageType.ERROR, e.getMessage());
        }
    }

    private <T> T buildProxyResponse(Request request) throws IOException {
        Response response = flinkProxyClient.newCall(request).execute();
        try {
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    String resp = response.body().string();
                    return buildFlinkResponse(resp);
                }
                return null;
            }
        } catch (Exception e) {
            throw BaseException.newException(MessageType.ERROR, e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
        throw BaseException.newException(MessageType.ERROR, response.message());
    }

    public <T> T buildFlinkResponse(String response) {
        if (StringUtils.isBlank(response)) return null;
        FlinkResponse<T> flinkResponse = JSONObject.parseObject(response,
                new TypeReference<FlinkResponse<T>>() {
                });
        Integer code = flinkResponse.getCode();
        if (code != 0) {
            throw BaseException.newException(MessageType.ERROR, flinkResponse.getMessage());
        }
        return flinkResponse.getDetail();
    }


}
