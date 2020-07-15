package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RestartGroupInstanceRequest {
    @ApiModelProperty(value = "发布组ID")
    private Long groupId;
    @ApiModelProperty(value = "实例名称(用英文逗号分隔)")
    private String name;
    @ApiModelProperty(value = "是否启用catagent")
    private Boolean enableCatAgent;
    @ApiModelProperty(value = "是否启用sentinelagent")
    private Boolean enableSentinelAgent;
}
