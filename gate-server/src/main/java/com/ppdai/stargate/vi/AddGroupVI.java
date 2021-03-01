package com.ppdai.stargate.vi;

import lombok.Data;

import java.util.List;

@Data
public class AddGroupVI {
    private String env;

    private String appId;

    private String appName;

    private String releaseTarget;

    private String instanceSpec;

    private Integer instanceCount;

    private Integer portCount;

    private String zone;

    private String staticResources;

    private String hadoopConfig;

    private String checkpoint;

    private List<String> zones;
}
