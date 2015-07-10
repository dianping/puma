package com.dianping.puma.biz.entity.sync.relation;

import com.dianping.puma.biz.entity.sync.mapping.DatabaseMapping;

public class TaskMappingEntity {

	private int id;

	private int taskId;

	private DatabaseMapping mapping;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public DatabaseMapping getMapping() {
		return mapping;
	}

	public void setMapping(DatabaseMapping mapping) {
		this.mapping = mapping;
	}
}
