package com.ppdai.stargate.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DnsDto {
    private Long id;

    private String name;

    private String type;

    private String content;

    private String envId;

    private Integer ttl = 60;

    public Date insertTime;

    public Date updateTime;
}
