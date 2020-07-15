package com.ppdai.stargate.vo;

import lombok.Data;

@Data
public class JobStatusVO {
    private String environment;
    private long runningCount;
    private long successCount;
    private long failCount;
}
