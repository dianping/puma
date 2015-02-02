package com.dianping.puma.core.server.model;

public enum ServerTaskActionStatus {
	NONE("默认值"),
	START("第一次运行"),
	STOP("停止"),
	RESTART("重新运行"),
	ADD("添加"),
	DELETE("删除"),
	UPDATE("修改");
	
	private final String desc;
	
	private ServerTaskActionStatus(String desc){
		this.desc=desc;
	}

	public String getDesc() {
		return desc;
	}
	
}
