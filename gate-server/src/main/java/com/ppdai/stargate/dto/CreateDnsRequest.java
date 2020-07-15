package com.ppdai.stargate.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateDnsRequest {
    private List<DnsDto> records;
}
