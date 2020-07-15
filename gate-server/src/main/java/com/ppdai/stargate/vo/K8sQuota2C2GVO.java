package com.ppdai.stargate.vo;

import io.kubernetes.client.custom.Quantity;

public class K8sQuota2C2GVO implements K8sQuotaVO {
    @Override
    public Quantity getRequestCpu() {
        return new Quantity("2");
    }

    @Override
    public Quantity getRequestMemory() {
        return new Quantity("2Gi");
    }

    @Override
    public Quantity getLimitCpu() {
        return new Quantity("2");
    }

    @Override
    public Quantity getLimitMemory() {
        return new Quantity("2Gi");
    }

    @Override
    public String getScope() {
        return "small";
    }

    @Override
    public String getJavaOpts() {
        return "-Xms1024m -Xmx1024m";
    }
}

