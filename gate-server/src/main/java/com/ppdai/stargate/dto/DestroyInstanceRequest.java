package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DestroyInstanceRequest {
    @ApiModelProperty(value = "实例名称(可取私有云资源ID)")
    private String name;
}
