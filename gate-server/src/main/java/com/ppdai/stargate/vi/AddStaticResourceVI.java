package com.ppdai.stargate.vi;

import lombok.Data;

@Data
public class AddStaticResourceVI {
    private String appId;
    private String env;
    private String spec;
    private Integer number;
    private String zone;
}
