package com.ppdai.stargate.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ImageVO {

    Long id;
    String name;
    String orgName;
    String repoName;
    String appName;
    String version;
    Date createdAt;
    Date updatedAt;
    Date deployAt;
}
