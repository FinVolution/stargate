package com.ppdai.stargate.dto;

import com.ppdai.stargate.po.ResourceEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CreateResourceResponse extends AbstractResponse {
    @ApiModelProperty(value = "资源")
    private ResourceEntity resource;

    public CreateResourceResponse(int code, String msg) {
        super(code, msg);
    }
}
