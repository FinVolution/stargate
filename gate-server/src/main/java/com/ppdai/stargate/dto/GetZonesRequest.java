package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GetZonesRequest {
    @ApiModelProperty(value = "应用名称")
    private String appName;

    @ApiModelProperty(value = "环境名称")
    private String env;
}
