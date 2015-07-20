package com.dianping.puma.server.container;

import java.util.List;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.taskexecutor.TaskExecutor;

public interface TaskContainer {

	public TaskExecutor get(String taskId);

	public List<TaskExecutor> getAll();

	public EventStorage getTaskStorage(String database);

	public void create(String name, PumaTaskEntity task);

	public void update(String name, PumaTaskEntity oriTask, PumaTaskEntity task);

	public void delete(String name, PumaTaskEntity task);

	public void start(String name);

	public void stop(String name);
}
