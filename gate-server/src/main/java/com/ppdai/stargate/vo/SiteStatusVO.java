package com.ppdai.stargate.vo;

import lombok.Data;

import java.util.List;

@Data
public class SiteStatusVO {

    String environment;
    String appId;
    String appName;
    String owner;
    String domain;
    Boolean enableStaticResource;
    List<String> zones;

}
