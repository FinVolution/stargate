package com.ppdai.stargate.vo;

import io.kubernetes.client.custom.Quantity;

public class K8sQuota2C4GVO implements K8sQuotaVO {
    @Override
    public Quantity getRequestCpu() {
        return new Quantity("2");
    }

    @Override
    public Quantity getRequestMemory() {
        return new Quantity("4Gi");
    }

    @Override
    public Quantity getLimitCpu() {
        return new Quantity("2");
    }

    @Override
    public Quantity getLimitMemory() {
        return new Quantity("4Gi");
    }

    @Override
    public String getScope() {
        return "medium";
    }

    @Override
    public String getJavaOpts() {
        return null;
    }
}
