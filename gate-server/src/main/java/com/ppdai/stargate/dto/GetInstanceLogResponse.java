package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GetInstanceLogResponse extends AbstractResponse {
    @ApiModelProperty(value = "实例输出的stdout日志")
    private String log;

    public GetInstanceLogResponse(int code, String msg) {
        super(code, msg);
    }
}
