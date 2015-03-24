package com.dianping.puma.server;

import com.dianping.puma.core.monitor.event.PumaTaskControllerEvent;
import com.dianping.puma.core.monitor.event.PumaTaskOperationEvent;

import java.util.List;

public interface TaskExecutorContainer {

	public void init();
	/*
	public TaskExecutor construct(PumaTask pumaTask) throws Exception;

	public ConcurrentHashMap<String,TaskExecutor> constructServers() throws Exception;
	*/
	public TaskExecutor get(String taskId);

	public List<TaskExecutor> getAll();

	public void startExecutor(final TaskExecutor taskExecutor) throws Exception;

	public void stopExecutor(TaskExecutor taskExecutor) throws Exception;

	public void submit(TaskExecutor taskExecutor) throws Exception;

	public void withdraw(TaskExecutor taskExecutor) throws Exception;

	public void stopServers();

	public void pauseEvent(PumaTaskControllerEvent event);

	public void resumeEvent(PumaTaskControllerEvent event);

	public void createEvent(PumaTaskOperationEvent event);

	public void updateEvent(PumaTaskOperationEvent event);

	public void removeEvent(PumaTaskOperationEvent event);

	public void prolongEvent(PumaTaskOperationEvent event);

	public String getPumaServerName();
}
