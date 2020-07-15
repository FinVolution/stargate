package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeployGroupInstanceRequest {
    @ApiModelProperty(value = "发布组ID")
    private Long groupId;
    @ApiModelProperty(value = "部署镜像")
    private String image;
    @ApiModelProperty(value = "静态资源(格式为zone@ip 用英文逗号分隔)")
    private String staticResources;
    @ApiModelProperty(value = "部署实例数")
    private Integer instanceCount;
    @ApiModelProperty(value = "部署区域")
    private String zone;
    @ApiModelProperty(value = "是否启用catagent")
    private Boolean enableCatAgent;
    @ApiModelProperty(value = "是否启用sentinelagent")
    private Boolean enableSentinelAgent;
}
