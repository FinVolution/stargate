package com.ppdai.stargate.service.flink.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionClusterArgs {

    private String clusterId;
    private String k8sMasterUrl;
    private String configFile;
    private String containerImage;
    private String entryPath;
    private String flinkConfDir;
    private String flinkLogDir;
    private Integer jobManagerCpu;
    private String jobManagerServiceAccount;
    private String namespace;
    private String restServiceExposedType;
    private String serviceCreateTimeout;
    private Integer taskManagerCpu;
    private String jobManagerHeapSize;
    private String jobManagerMemoryProcessSize;
    private Map<String, String> jobManagerLabels;
    private Map<String, String> taskManagerLabels;
    private String taskManagerMemorySize;
    private int taskManagerNumberOfTaskSlots;
    private int parallelism;
    private String cmd;
    private String savepointDirectory;
    private String hadoopConfigMapName;
    private String variable;
    private String department;


}