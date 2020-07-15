package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GetAllInstanceRequest {
    @ApiModelProperty(value = "环境名称")
    private String env;
}
