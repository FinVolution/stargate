package com.ppdai.stargate.vo;

import lombok.Data;

import java.util.List;

@Data
public class AppOnlineStatusVO {
    private Integer appCount;
    private List<String> appList;
}
