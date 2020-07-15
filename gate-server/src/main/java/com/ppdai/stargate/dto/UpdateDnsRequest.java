package com.ppdai.stargate.dto;

import lombok.Data;

@Data
public class UpdateDnsRequest {
    private Long id;
    private String name;
    private String type;
    private String content;
    private String envId;
}
