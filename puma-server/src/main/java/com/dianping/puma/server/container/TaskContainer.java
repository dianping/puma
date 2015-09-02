package com.dianping.puma.server.container;

import com.dianping.puma.taskexecutor.task.DatabaseTask;

import java.util.Map;

public interface TaskContainer {

	public Map<String, DatabaseTask> getAll();

	public void create(DatabaseTask databaseTask);

	public void update(DatabaseTask databaseTask);

	public void remove(String database);
}
