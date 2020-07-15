package com.ppdai.stargate.vo;

import lombok.Data;

import java.util.List;

@Data
public class TransferInstanceVO {
    private String env;
    private String hostIp;
    private List<Long> instanceIds;
}
