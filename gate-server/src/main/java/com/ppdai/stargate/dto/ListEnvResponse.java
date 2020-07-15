package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ListEnvResponse extends AbstractResponse {
    @ApiModelProperty(value = "环境列表")
    private List<String> envs;

    public ListEnvResponse(int code, String msg) {
        super(code, msg);
    }

}
