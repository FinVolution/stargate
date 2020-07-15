package com.ppdai.stargate.vo;

import lombok.Data;

@Data
public class ExecCommandVO {
    private String type;
    private String args;
    private String instance;
}
