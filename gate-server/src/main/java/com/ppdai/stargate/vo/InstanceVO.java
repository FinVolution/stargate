package com.ppdai.stargate.vo;

import lombok.Data;

import java.util.Date;

@Data
public class InstanceVO {

    private Long id;
    private String name;
    private Long groupId;
    private String groupName;
    private String releaseTarget;
    private String releaseVersion;
    private String instanceSpec;
    private String appId;
    private String appName;
    private String appType;
    private Integer weight;
    private Long slbSiteServerId;
    private String ip;
    private String port;
    private Boolean hasPulledIn;
    private String zone;
    private Date releaseTime;
    private Boolean isStatic;

    /**
     * 状态标志位
     * a) 0 该标志位忽略
     * b) 1 该标志位为Up
     * c) -1 该标志位为Down
     * <p>
     * 综合状态 status
     * 健康检查 opsHealthUp
     * 发布系统上下线 opsPulledIn
     * 手动上下线 opsManualUp
     * 强制上线 opsForceUp
     */
    private Integer status;
    private Integer opsHealthUp;
    private Integer opsPulledIn;
    private Integer opsManualUp;
    private Integer opsForceUp;

    /**
     * 容器信息
     */
    private String containerStatus;
    private String containerId;
    private String containerFullId;
    private String agentHost;
    private String env;
    private String department;
    private String containerConsoleUrl;
    private Boolean ready;
}
