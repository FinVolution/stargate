package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ChangeResourceTypeResponse extends AbstractResponse {
    @ApiModelProperty(value = "转换资源数")
    private int count;

    public ChangeResourceTypeResponse(int code, String msg) {
        super(code, msg);
    }
}
