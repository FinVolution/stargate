package com.ppdai.stargate.controller.request;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * Created by chenlang on 2020/8/31
 **/
@Data
public class FlinkDeployRequest {
    private Long groupId;
    private String image;
    private String savepointPath;
    @Size(max = 200, message = "savepointPaht长度不能超过200个字符")
    private boolean savepointSwitch;
    private String cmd;
    private String variable;
    private int instanceCount;
    private String releaseTarget;
}
