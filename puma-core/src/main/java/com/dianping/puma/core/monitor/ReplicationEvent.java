package com.dianping.puma.core.monitor;

public abstract class ReplicationEvent extends Event {

	protected String replicationServerName;

	public String getReplicationServerName() {
		return replicationServerName;
	}

	public void setReplicationServerName(String replicationServerName) {
		this.replicationServerName = replicationServerName;
	}
}
