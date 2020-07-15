package com.ppdai.stargate.vi;

import com.ppdai.stargate.constant.OperatorType;
import lombok.Data;

@Data
public class PullInstanceVI {
    private Long groupId;
    private OperatorType operatorType;
    private String instanceNames;
}
