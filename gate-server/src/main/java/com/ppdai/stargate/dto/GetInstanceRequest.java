package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GetInstanceRequest {
    @ApiModelProperty(value = "实例名称(用英文逗号分隔)")
    private String name;
}
