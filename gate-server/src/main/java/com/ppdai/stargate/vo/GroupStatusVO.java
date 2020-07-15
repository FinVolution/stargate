package com.ppdai.stargate.vo;

import lombok.Data;

import java.util.List;

@Data
public class GroupStatusVO {

    String environment;
    String appId;
    String appName;
    String owner;
    String domain;
    Long groupId;
    String groupName;
    String releaseVersion;
    String releaseTarget;
    String instanceSpec;
    Boolean enableStaticResource;
    List<String> zones;

}
