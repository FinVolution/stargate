package com.ppdai.stargate.constant;

public enum OperatorType {

	CREATE_GROUP("创建发布组"),
	REMOVE_GROUP("删除发布组"),
	EXPAND_GROUP("添加实例"),
	REDUCE_GROUP("删除实例"),
	UPDATE_INSTANCE("更新实例"),
	RESTART_INSTANCE("重启实例"),
	PULL_IN("拉入流量"),
	PULL_OUT("拉出流量"),
	ROLLING_GROUP("滚动发布"),
    RECOVER_INSTANCE_FRONT("恢复实例上"),
    RECOVER_INSTANCE_REAR("恢复实例下"),
	DEPLOY_INSTANCE("部署实例"),
	DESTROY_INSTANCE("销毁实例"),
	UP_INSTANCE("上线实例"),
	DOWN_INSTANCE("下线实例"),
	AUTO_UPDATE_INSTANCE("自动更新实例"),
	EXEC_COMMAND("执行命令");

    String description;

	OperatorType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
