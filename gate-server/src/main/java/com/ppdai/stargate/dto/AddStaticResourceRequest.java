package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AddStaticResourceRequest {
    @ApiModelProperty(value = "应用ID")
    private String appId;
    @ApiModelProperty(value = "环境")
    private String env;
    @ApiModelProperty(value = "规格")
    private String spec;
    @ApiModelProperty(value = "数量")
    private Integer number;
    @ApiModelProperty(value = "部署区域")
    private String zone;
}
