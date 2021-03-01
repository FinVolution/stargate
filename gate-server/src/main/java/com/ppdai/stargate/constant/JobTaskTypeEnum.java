package com.ppdai.stargate.constant;

public enum JobTaskTypeEnum {


    removeGroup("删除发布组"),
    DEPLOY_ONE("部署容器"),
    DESTROY_ONE("销毁容器"),
    UPDATE_ONE("更新容器"),
    RESTART_ONE("重启容器"),
    REGISTRY_ONE("注册实例"),
    DEREGISTRY_ONE("注销实例"),
    UP_ONE("拉入流量"),
    DOWN_ONE("拉出流量"),
    ADD_HC_ONE("添加健康检查"),
    REMOVE_HC_ONE("删除健康检查"),
    NOOP("等待"),
    RECOVER_FLINK_JOB("恢复Job"),
    DEPLOY_FLINKJOB("部署FlinkJob"),
    STOP_FLINKJOB("停止FlinkJob"),
    DESTROY_FLINKJOB("销毁FlinkJob"),
    SYNC_INSTANCE_JOB("同步实例任务");

    String description;

    JobTaskTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
