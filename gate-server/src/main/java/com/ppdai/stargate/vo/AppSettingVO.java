package com.ppdai.stargate.vo;

import lombok.Data;

@Data
public class AppSettingVO {
    private String appId;
    private String appName;
    private Boolean enableMq;
    private Boolean enableJob;
    private Boolean enableVersion;
}
