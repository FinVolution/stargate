package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class QueryInstanceRequest {
    @ApiModelProperty(value = "实例ip")
    private String ip;
}
