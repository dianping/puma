package com.dianping.puma.server.container;

import com.dianping.puma.storage.channel.ReadChannel;
import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.task.DatabaseTask;
import com.dianping.puma.taskexecutor.task.InstanceTask;

import java.util.List;
import java.util.Map;

public interface TaskContainer {

	public ReadChannel getTaskStorage(String database);

	public Map<String, DatabaseTask> getDatabaseTasks();

	public Map<String, TaskExecutor> getMainExecutors();

	public Map<String, List<TaskExecutor>> getTempExecutors();

	public void create(InstanceTask instanceTask);

	public void create(DatabaseTask databaseTask);

	public void update(DatabaseTask databaseTask);

	public void remove(String database);

	public void merge(TaskExecutor mainTaskExecutor, TaskExecutor tempTaskExecutor);

	public void upgrade(TaskExecutor taskExecutor);

	public void start(TaskExecutor taskExecutor);

	public void stop(TaskExecutor taskExecutor);

	public TaskExecutor getExecutor(String database);
}
