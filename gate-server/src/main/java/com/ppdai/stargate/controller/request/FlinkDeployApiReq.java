package com.ppdai.stargate.controller.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by chenlang on 2020/9/11
 **/
@Data
public class FlinkDeployApiReq {

    @NotEmpty(message = "appId不能为空")
    private String appId;
    private String appName;
    @NotEmpty(message = "环境不能为空")
    private String env;
    private String image;
    private String imageId;
    @Length(max = 248, message = "启动参数不能超过1024个字符")
    private String runCmd;
    private String variable;
    private int taskCount;
    private String taskQuota;
    private String savepointPath;
    private double taskSlots;
    private double parallelism;

}
