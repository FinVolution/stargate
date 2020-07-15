package com.ppdai.stargate.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class InstanceV2VO {
    @ApiModelProperty(value = "实例ID")
    private Long id;
    @ApiModelProperty(value = "实例名称")
    private String name;
    @ApiModelProperty(value = "环境名称")
    private String env;
    @ApiModelProperty(value = "应用ID")
    private String appId;
    @ApiModelProperty(value = "应用名")
    private String appName;
    @ApiModelProperty(value = "发布组ID")
    private Long groupId;
    @ApiModelProperty(value = "实例IP")
    private String instanceIp;
    @ApiModelProperty(value = "实例端口")
    private Integer port;
    @ApiModelProperty(value = "宿主机IP")
    private String hostIp;
    @ApiModelProperty(value = "镜像")
    private String image;
    @ApiModelProperty(value = "发布规格")
    private String spec;
    @ApiModelProperty(value = "环境变量")
    private String envVars;
    @ApiModelProperty(value = "部署区域")
    private String zone;
    @ApiModelProperty(value = "发布时间")
    private Date releaseTime;
    @ApiModelProperty(value = "创建时间")
    private Date insertTime;
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    @ApiModelProperty(value = "创建人")
    private String insertBy;

    @ApiModelProperty(value = "当前是否被拉入流量(人为操作)")
    private Boolean hasPulledIn;
    @ApiModelProperty(value = "当前是否接入流量(从consul获取)")
    private Boolean opsPulledIn;

    @ApiModelProperty(value = "容器状态")
    private String containerStatus;
    @ApiModelProperty(value = "容器短ID")
    private String containerId;
    @ApiModelProperty(value = "容器长ID")
    private String containerFullId;
    @ApiModelProperty(value = "终端url")
    private String containerConsoleUrl;
    @ApiModelProperty(value = "实例是否启动成功")
    private Boolean ready;

    @ApiModelProperty(value = "是否为静态资源")
    private Boolean isStatic;
    @ApiModelProperty(value = "64位表示的开关状态")
    private Long switchBitset;
}
