package com.ppdai.stargate.vi;

import lombok.Data;

@Data
public class PodHttpReadinessProbeVI {
    private String path = "/hs";
    private Integer port = 8080;
    private Integer failureThreshold = 12;
    private Integer initialDelaySeconds = 60;
    private Integer periodSeconds = 5;
    private Integer timeoutSeconds =3;
    private Integer successThreshold = 3;
}
