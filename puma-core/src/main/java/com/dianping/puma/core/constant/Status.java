package com.dianping.puma.core.constant;

public enum Status {
	INITIALIZING("初始化中"),
	WAITING("等待中"),
	PREPARING("准备中"),
	RUNNING("运行中"),
	SUSPENDED("已暂停"),
	STOPPING("停止中"),
	STOPPED("已停止"),
	SUCCESS("结束-成功"),
	CONNECTED("已连接"),
	RECONNECTING("重新连接"),
	DUMPING("DUMP中"),
	LOADING("载入中"),
	FAILED("已失败"),
	DISCONNECTED("失去连接");

	private final String desc;

	private Status(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
}
