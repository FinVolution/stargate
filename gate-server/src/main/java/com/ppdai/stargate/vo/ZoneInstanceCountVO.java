package com.ppdai.stargate.vo;

import lombok.Data;

@Data
public class ZoneInstanceCountVO {
    private String zone;
    private Integer freeCount;
    private Integer usedCount;
}
