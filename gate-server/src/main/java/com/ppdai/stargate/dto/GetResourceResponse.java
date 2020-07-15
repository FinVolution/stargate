package com.ppdai.stargate.dto;

import com.ppdai.stargate.po.ResourceEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetResourceResponse extends AbstractResponse {
    @ApiModelProperty(value = "资源列表")
    private List<ResourceEntity> resources;

    public GetResourceResponse(int code, String msg) {
        super(code, msg);
    }
}
