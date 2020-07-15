package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class GetEnvImagesResponse extends AbstractResponse {

    @Data
    public static class ImageDTO {
        @ApiModelProperty(value = "镜像版本")
        private String image;
        @ApiModelProperty(value = "最近部署时间")
        private Date deployTime;
    }

    @ApiModelProperty(value = "镜像版本列表")
    private List<ImageDTO> images;

    public GetEnvImagesResponse(int code, String msg) {
        super(code, msg);
    }
}
