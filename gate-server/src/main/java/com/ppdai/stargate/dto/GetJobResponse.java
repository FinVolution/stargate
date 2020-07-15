package com.ppdai.stargate.dto;

import com.ppdai.stargate.vo.JobVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GetJobResponse extends AbstractResponse {
    @ApiModelProperty(value = "任务信息")
    private JobVO job;

    public GetJobResponse(int code, String msg) {
        super(code, msg);
    }
}
