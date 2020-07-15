package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeleteResourceRequest {
    @ApiModelProperty(value = "资源ID")
    private Long resourceId;
}
