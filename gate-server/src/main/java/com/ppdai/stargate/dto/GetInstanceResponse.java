package com.ppdai.stargate.dto;

import com.ppdai.stargate.vo.InstanceV2VO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetInstanceResponse extends AbstractResponse {
    @ApiModelProperty(value = "实例信息")
    private List<InstanceV2VO> instances;

    public GetInstanceResponse(int code, String msg) {
        super(code, msg);
    }
}
