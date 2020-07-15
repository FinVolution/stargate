package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DownGroupInstanceResponse extends AbstractResponse {
    @ApiModelProperty(value = "任务ID")
    private long jobId;

    public DownGroupInstanceResponse(int code, String msg) {
        super(code, msg);
    }
}
