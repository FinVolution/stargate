package com.ppdai.stargate.dto;

import com.ppdai.stargate.vo.InstanceV2VO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DownInstanceResponse extends AbstractResponse {
    @ApiModelProperty(value = "任务ID")
    private long jobId;
    @ApiModelProperty(value = "实例信息")
    private InstanceV2VO instance;

    public DownInstanceResponse(int code, String msg) {
        super(code, msg);
    }
}
