package com.ppdai.stargate.vi;

import lombok.Data;

@Data
public class UpdateInstanceVI {
    private Long groupId;
    private String appName;
    private String instanceNames;
    private String image;
}
