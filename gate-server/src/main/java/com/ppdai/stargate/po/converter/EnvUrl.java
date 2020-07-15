package com.ppdai.stargate.po.converter;

import lombok.Data;

/**
 * mapping to json string in table column
 */
@Data
public class EnvUrl {
    private String envName;
    private String url;
}
