package com.dianping.puma.syncserver.job.load.status;

public enum LoaderStatus {

	RUNNING("running"),
	PAUSED("paused"),
	STOPPED("stopped"),
	FAILED("failed");

	private final String desc;

	private LoaderStatus(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
}
