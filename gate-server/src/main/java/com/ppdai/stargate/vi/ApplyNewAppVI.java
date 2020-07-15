package com.ppdai.stargate.vi;

import lombok.Data;

@Data
public class ApplyNewAppVI {
    private String appId;
    private String appName;
    private String instanceCount;
    private String instanceSpec;
    private String domain;
    private String appType;
    private String serviceType;
    private String level;
    private String department;
}
