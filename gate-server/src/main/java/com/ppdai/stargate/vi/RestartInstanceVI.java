package com.ppdai.stargate.vi;

import lombok.Data;

@Data
public class RestartInstanceVI {
    private Long groupId;
    private String instanceNames;
}
