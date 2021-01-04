package com.ppdai.stargate.service.flink.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenlang on 2020/7/17
 **/
@Data
public class BaseFlinkReq {
    private String clusterId;
    private String k8sMasterUrl;
    private String namespace;
    private Map<String,String> labels = new HashMap<>();
}
