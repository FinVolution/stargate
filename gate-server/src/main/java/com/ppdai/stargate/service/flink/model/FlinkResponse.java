package com.ppdai.stargate.service.flink.model;

import lombok.Data;

/**
 * Created by chenlang on 2020/6/11
 **/
@Data
public class FlinkResponse<T> {
    private Integer code;
    private String message;
    private T detail;

}
