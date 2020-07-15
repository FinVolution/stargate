package com.ppdai.stargate.vo;

import io.kubernetes.client.custom.Quantity;

public class K8sQuota4C8GVO implements K8sQuotaVO {
    @Override
    public Quantity getRequestCpu() {
        return new Quantity("4");
    }

    @Override
    public Quantity getRequestMemory() {
        return new Quantity("8Gi");
    }

    @Override
    public Quantity getLimitCpu() {
        return new Quantity("4");
    }

    @Override
    public Quantity getLimitMemory() {
        return new Quantity("8Gi");
    }

    @Override
    public String getScope() {
        return "large";
    }

    @Override
    public String getJavaOpts() {
        return null;
    }
}
