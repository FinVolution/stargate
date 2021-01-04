package com.ppdai.stargate.service.flink.model;

public enum FlinkJobStatus {

    CREATED("创建"),
    RUNNING("运行中"),
    FAILING("失败"),
    FAILED("失败"),
    CANCELLING("取消中"),
    CANCELED("取消"),
    FINISHED("完成"),
    RESTARTING("重启"),
    SUSPENDING("SUSPENDING"),
    SUSPENDED("SUSPENDED"),
    RECONCILING("RECONCILING");
    private String name;

    FlinkJobStatus(String name) {
        this.name = name;
    }
}
