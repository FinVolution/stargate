package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ReleaseResourceRequest {
    @ApiModelProperty(value = "实例名")
    private String podName;
}
