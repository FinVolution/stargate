package com.ppdai.stargate.vo;

import lombok.Data;

@Data
public class ZoneResourceCountVO {
    private String zone;
    private Integer usedCount;
    private Integer allotCount;
}