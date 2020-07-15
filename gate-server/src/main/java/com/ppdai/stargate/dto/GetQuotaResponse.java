package com.ppdai.stargate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetQuotaResponse extends AbstractResponse {

    @Data
    public static class QuotaDTO {
        @ApiModelProperty(value = "配额规格")
        private String spec;
        @ApiModelProperty(value = "总数")
        private long total;
        @ApiModelProperty(value = "已使用数")
        private long used;
        @ApiModelProperty(value = "可使用数")
        private long free;
    }

    @ApiModelProperty(value = "配额列表")
    private List<QuotaDTO> quotas;

    public GetQuotaResponse(int code, String msg) {
        super(code, msg);
    }
}
