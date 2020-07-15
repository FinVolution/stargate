package com.ppdai.stargate.vo;

import lombok.Data;

import java.util.Date;

@Data
public class EnvVO {

    Long id;
    Long cmdbEnvId;
    String name;
    String consul;
    String nginx;
    String dns;
    String dockeryard;
    Boolean isInUse;
    Boolean enableHa;
    String description;
    Date insertTime;

}
