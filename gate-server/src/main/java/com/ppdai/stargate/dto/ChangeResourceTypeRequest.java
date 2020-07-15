package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ChangeResourceTypeRequest {
    @ApiModelProperty(value = "应用ID")
    private String appId;
    @ApiModelProperty(value = "环境")
    private String env;
    @ApiModelProperty(value = "当前资源类型")
    private Boolean isStatic;
}
