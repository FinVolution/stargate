package com.ppdai.stargate.vi;

import com.ppdai.stargate.vo.K8sQuotaVO;
import lombok.Data;

@Data
public class AppPodTemplateVI {
    private String namespace;
    private String appId;
    private String appName;
    private String image;
    private K8sQuotaVO k8sQuotaVO;
    private String dns;
    private String podName;
    private String podIp;
    private Integer port;
    private String envJson;
    private String sysctls;
    private Long flags;
    private PodHttpReadinessProbeVI podHttpReadinessProbeVI = new PodHttpReadinessProbeVI();
}
