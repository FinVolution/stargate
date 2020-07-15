package com.ppdai.stargate.dto;

import com.ppdai.stargate.vo.InstanceV3VO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetAllInstanceResponse extends AbstractResponse {
    @ApiModelProperty(value = "实例信息")
    private List<InstanceV3VO> instances;

    public GetAllInstanceResponse(int code, String msg) {
        super(code, msg);
    }
}
