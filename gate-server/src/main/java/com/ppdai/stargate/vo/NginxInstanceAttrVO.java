package com.ppdai.stargate.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class NginxInstanceAttrVO {
    private String name;

    private Integer weight = 1;

    @JSONField(name="max_fails")
    private Integer maxFails = 2;

    @JSONField(name="fail_timeout")
    private Integer failTimeout = 10;
    private Integer down = 0;
    private Integer backup = 0;
}
