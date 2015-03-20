package com.dianping.puma.core.monitor;

public class SyncTaskEvent extends Event {

	private String syncServerName;

	@Override
	public String getSyncServerName() {
		return syncServerName;
	}

	@Override
	public void setSyncServerName(String syncServerName) {
		this.syncServerName = syncServerName;
	}
}
