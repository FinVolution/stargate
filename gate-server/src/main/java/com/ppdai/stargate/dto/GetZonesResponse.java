package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetZonesResponse extends AbstractResponse {
    @ApiModelProperty(value = "zone列表")
    private List<String> zones;

    public GetZonesResponse(int code, String msg) {
        super(code, msg);
    }
}
