package com.dianping.puma.server.operator;

import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.task.DatabaseTask;
import com.dianping.puma.taskexecutor.task.InstanceTask;

public interface TaskOperator {

	public TaskExecutor create(InstanceTask instanceTask);

	public TaskExecutor union(TaskExecutor executor, DatabaseTask databaseTask);

	public TaskExecutor complement(TaskExecutor executor, String database);

	public TaskExecutor union(TaskExecutor executor0, TaskExecutor executor1);
}
