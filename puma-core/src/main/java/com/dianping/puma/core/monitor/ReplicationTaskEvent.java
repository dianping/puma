package com.dianping.puma.core.monitor;

import com.dianping.puma.core.replicate.model.task.Type;

public class ReplicationTaskEvent extends Event {

	private Type type;

	private long taskId;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
}
