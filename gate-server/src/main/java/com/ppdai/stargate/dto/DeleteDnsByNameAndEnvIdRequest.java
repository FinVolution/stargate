package com.ppdai.stargate.dto;

import lombok.Data;

@Data
public class DeleteDnsByNameAndEnvIdRequest {
    private String name;
    private String envId;
}
