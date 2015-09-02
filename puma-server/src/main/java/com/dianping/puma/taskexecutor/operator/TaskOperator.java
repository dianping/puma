package com.dianping.puma.taskexecutor.operator;

import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.task.DatabaseTask;

public interface TaskOperator {

	public TaskExecutor union(TaskExecutor executor, DatabaseTask databaseTask);

	public TaskExecutor complement(TaskExecutor executor, String database);

	public TaskExecutor union(TaskExecutor executor0, TaskExecutor executor1);
}
