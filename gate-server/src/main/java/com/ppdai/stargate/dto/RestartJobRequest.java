package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RestartJobRequest {
    @ApiModelProperty(value = "任务ID")
    private Long jobId;
}
