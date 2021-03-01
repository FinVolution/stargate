package com.ppdai.stargate.controller.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class FlinkGroupApiReq {

    @NotEmpty(message = "appId不能为空")
    private String appId;
    @NotEmpty(message = "环境不能为空")
    private String env;
    private String taskQuota;
    private String hadoopCluster;
    private int taskCount;
    private double taskSlots;
    private double parallelism;
    private String runCmd;
    private String variable;
    private String checkpoint;

}
