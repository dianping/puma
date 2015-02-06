package com.dianping.puma.core.monitor;

import java.util.List;

import com.dianping.puma.core.replicate.model.task.TaskExecutorStatus;

public class ReplicationTaskReportEvent extends ReplicationEvent {
	
	private List<TaskExecutorStatus> statusList;

	public void setStatusList(List<TaskExecutorStatus> statusList) {
		this.statusList = statusList;
	}

	public List<TaskExecutorStatus> getStatusList() {
		return statusList;
	}

}
