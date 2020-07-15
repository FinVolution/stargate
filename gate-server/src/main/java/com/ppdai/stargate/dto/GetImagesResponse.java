package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class GetImagesResponse extends AbstractResponse {

    @ApiModelProperty(value = "镜像版本列表")
    private List<String> images;

    public GetImagesResponse(int code, String msg) {
        super(code, msg);
    }
}
