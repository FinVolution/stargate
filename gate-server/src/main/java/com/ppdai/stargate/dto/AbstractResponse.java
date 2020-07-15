package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public abstract class AbstractResponse {
    @ApiModelProperty(value = "0:成功, -1:失败")
    private int code;
    @ApiModelProperty(value = "详细信息")
    private String msg;

    public AbstractResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
