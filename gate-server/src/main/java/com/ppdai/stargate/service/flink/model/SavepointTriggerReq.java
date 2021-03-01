package com.ppdai.stargate.service.flink.model;

import lombok.Data;

@Data
public class SavepointTriggerReq {

    private String namespace;
    private String k8sMasterUrl;
    private String clusterId;
    private boolean cancelJob;
    private String targetDirectory;
    private String jobId;
    private boolean destroy = false;

}