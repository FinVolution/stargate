package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UpdateGroupInstanceResponse extends AbstractResponse {
    @ApiModelProperty(value = "任务ID")
    private long jobId;

    public UpdateGroupInstanceResponse(int code, String msg) {
        super(code, msg);
    }
}
