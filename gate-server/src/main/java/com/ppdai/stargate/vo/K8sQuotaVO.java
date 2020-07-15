package com.ppdai.stargate.vo;

import io.kubernetes.client.custom.Quantity;

public interface K8sQuotaVO {
    /**
     * CPU配额
     * @return
     */
    Quantity getRequestCpu();

    /**
     * Memory配额
     * @return
     */
    Quantity getRequestMemory();

    /**
     * CPU配额
     * @return
     */
    Quantity getLimitCpu();

    /**
     * Memory配额
     * @return
     */
    Quantity getLimitMemory();

    /**
     * 告诉k8s需要从该scope扣除资源
     * @return
     */
    String getScope();

    /**
     * jvm参数
     * @return
     */
    String getJavaOpts();
}
