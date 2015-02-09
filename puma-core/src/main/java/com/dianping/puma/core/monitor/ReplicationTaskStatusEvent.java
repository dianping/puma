package com.dianping.puma.core.monitor;

import com.dianping.puma.core.model.replication.ReplicationTaskStatus;

import java.util.List;

public class ReplicationTaskStatusEvent extends ReplicationEvent {

	private List<ReplicationTaskStatus> replicationTaskStatuses;

	public List<ReplicationTaskStatus> getReplicationTaskStatuses() {
		return replicationTaskStatuses;
	}

	public void setReplicationTaskStatuses(List<ReplicationTaskStatus> replicationTaskStatuses) {
		this.replicationTaskStatuses = replicationTaskStatuses;
	}
}
