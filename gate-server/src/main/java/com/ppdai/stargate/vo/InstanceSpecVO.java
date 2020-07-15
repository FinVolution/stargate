package com.ppdai.stargate.vo;

import lombok.Data;

@Data
public class InstanceSpecVO {

    Long id;
    String name;
    Float cpu;
    Float memory;
    Float disk;

}
