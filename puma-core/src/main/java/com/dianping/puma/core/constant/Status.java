package com.dianping.puma.core.constant;

public enum Status {
	WAITING("等待中"),
	PREPARING("准备中"),
	RUNNING("运行中"),
	STOPPING("停止中"),
	STOPPED("已停止"),
	FAILED("已失败");

	private final String desc;

	private Status(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
}
