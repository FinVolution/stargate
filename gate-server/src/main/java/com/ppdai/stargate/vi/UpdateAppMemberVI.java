package com.ppdai.stargate.vi;

import lombok.Data;

@Data
public class UpdateAppMemberVI {
    private String appId;
    private String developers;
    private String testers;
}
