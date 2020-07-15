package com.ppdai.stargate.vi;

import com.ppdai.stargate.constant.OperatorType;
import lombok.Data;

@Data
public class ResizeGroupVI {
    private Long groupId;
    private String env;
    private String appId;
    private String appName;
    private OperatorType operatorType;
    private Integer instanceCount;
    private String staticResources;
    private String instanceNames;
    private String image;
    private String zone;
}
