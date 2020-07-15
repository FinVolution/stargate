package com.ppdai.stargate.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class ConsulInstanceVO {

    @JSONField(name="Key")
    private String key;

    @JSONField(name="Value")
    private String value;
}
