package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DestroyGroupInstanceResponse extends AbstractResponse {
    @ApiModelProperty(value = "任务ID")
    private long jobId;

    public DestroyGroupInstanceResponse(int code, String msg) {
        super(code, msg);
    }
}
