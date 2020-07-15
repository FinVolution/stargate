package com.ppdai.stargate.vi;

import lombok.Data;

@Data
public class ChangeAppQuotaVI {
    private String env;
    private String appId;
    private String appName;
    private String spec;
    private int number;
}
