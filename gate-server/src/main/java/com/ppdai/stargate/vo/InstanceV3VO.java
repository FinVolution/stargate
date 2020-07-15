package com.ppdai.stargate.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class InstanceV3VO {
    @ApiModelProperty(value = "发布组ID")
    private Long groupId;
    @ApiModelProperty(value = "环境名称")
    private String env;
    @ApiModelProperty(value = "部署区域")
    private String zone;
    @ApiModelProperty(value = "实例名称")
    private String name;
    @ApiModelProperty(value = "应用名")
    private String appName;
    @ApiModelProperty(value = "实例镜像")
    private String image;
    @ApiModelProperty(value = "实例规格")
    private String spec;
    @ApiModelProperty(value = "实例端口")
    private Integer port;
    @ApiModelProperty(value = "实例IP")
    private String instanceIp;
    @ApiModelProperty(value = "宿主机IP")
    private String hostIp;
    @ApiModelProperty(value = "cpu规格")
    private String cpu;
    @ApiModelProperty(value = "内存规格")
    private String memory;
    @ApiModelProperty(value = "容器状态")
    private String containerStatus;
    @ApiModelProperty(value = "健康检查状态")
    private Boolean ready;
    @ApiModelProperty(value = "启动时间")
    private Date startTime;
}
