package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UpdateInstanceRequest {
    @ApiModelProperty(value = "实例名称(可取私有云资源ID)")
    private String name;
    @ApiModelProperty(value = "更新镜像")
    private String image;
    @ApiModelProperty(value = "是否启用catagent")
    private Boolean enableCatAgent;
    @ApiModelProperty(value = "是否启用sentinelagent")
    private Boolean enableSentinelAgent;
}
