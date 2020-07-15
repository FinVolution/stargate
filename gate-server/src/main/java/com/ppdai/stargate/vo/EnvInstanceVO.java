package com.ppdai.stargate.vo;

import lombok.Data;

import java.util.Date;

@Data
public class EnvInstanceVO {
    private String env;
    private String name;
    private String appId;
    private String appName;
    private String department;
    private String spec;
    private String image;
    private String ip;
    private Integer port;
    private Boolean hasPulledIn;
    private Date releaseTime;
}
