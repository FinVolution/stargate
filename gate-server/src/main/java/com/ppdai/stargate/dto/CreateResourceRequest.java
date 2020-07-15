package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CreateResourceRequest {
    @ApiModelProperty(value = "应用ID")
    private String appId;
    @ApiModelProperty(value = "环境")
    private String env;
    @ApiModelProperty(value = "规格")
    private String spec;
    @ApiModelProperty(value = "实例名")
    private String podName;
    @ApiModelProperty(value = "是否静态")
    private Boolean isStatic;
}
