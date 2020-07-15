package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeployInstanceRequest {
    @ApiModelProperty(value = "实例名称(可取私有云资源ID)")
    private String name;
    @ApiModelProperty(value = "环境名称")
    private String env;
    @ApiModelProperty(value = "应用ID")
    private String appId;
    @ApiModelProperty(value = "启动端口")
    private Integer port;
    @ApiModelProperty(value = "部署规格")
    private String spec;
    @ApiModelProperty(value = "部署镜像")
    private String image;
    @ApiModelProperty(value = "环境变量(序列化成json字符串)")
    private String envVars;
    @ApiModelProperty(value = "部署区域")
    private String zone;
    @ApiModelProperty(value = "部署IP")
    private String ip;
    @ApiModelProperty(value = "命名空间")
    private String namespace;
    @ApiModelProperty(value = "组ID")
    private Long groupId = 0l;
}
