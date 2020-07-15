package com.ppdai.stargate.vo;

import lombok.Data;

@Data
public class InstanceCountVO {
    private String env;
    private String zone;
    private Integer instanceCount;
}
