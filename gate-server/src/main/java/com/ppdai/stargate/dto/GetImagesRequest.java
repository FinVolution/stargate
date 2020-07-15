package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GetImagesRequest {
    @ApiModelProperty(value = "应用名称")
    private String appName;
}
