package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeployGroupInstanceResponse extends AbstractResponse {
    @ApiModelProperty(value = "任务ID")
    private long jobId;

    public DeployGroupInstanceResponse(int code, String msg) {
        super(code, msg);
    }
}
