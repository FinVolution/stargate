package com.ppdai.stargate.vo;

import lombok.Data;

@Data
public class K8sInstanceVO {
    private String k8s;
    private String hostIp;
    private String ip;
    private String name;
}
