package com.ppdai.stargate.vo;

import lombok.Data;

@Data
public class AppQuotaStatusVO {
    private String spectypeName;
    private Long total;
    private Long usedCount;
    private Long freeCount;
}
