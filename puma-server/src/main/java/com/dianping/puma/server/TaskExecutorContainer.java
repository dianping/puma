package com.dianping.puma.server;

import java.util.concurrent.ConcurrentHashMap;

import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.monitor.PumaTaskControllerEvent;
import com.dianping.puma.core.monitor.PumaTaskOperationEvent;
import com.dianping.puma.core.monitor.ReplicationTaskStatusActionEvent;

public interface TaskExecutorContainer {

	public void init();

	public TaskExecutor construct(PumaTask pumaTask) throws Exception;

	public ConcurrentHashMap<String,TaskExecutor> constructServers() throws Exception;

	public void initContext(TaskExecutor taskExecutor);

	public void submit(TaskExecutor taskExecutor);

	public void withdraw(TaskExecutor taskExecutor);

	public boolean contain(String taskName);

	public boolean addServer(TaskExecutor taskExecutor);

	public void remove(String taskName);

	public void startExecutor(final TaskExecutor taskExecutor);

	public void stopExecutor(TaskExecutor taskExecutor);
	
	public void stopServers();
	
	//public ConcurrentHashMap<Long,Server> getServers();
	
	public void pauseEvent(PumaTaskControllerEvent event) throws Exception;
	
	public void resumeEvent(PumaTaskControllerEvent event) throws Exception;

	public void createEvent(PumaTaskOperationEvent event) throws Exception;

	public void updateEvent(PumaTaskOperationEvent event) throws Exception;

	public void removeEvent(PumaTaskOperationEvent event) throws Exception;

	/*
	public void addEvent(ReplicationTaskEvent event);
	
	public void deleteEvent(ReplicationTaskEvent event);
	
	public void updateEvent(ReplicationTaskEvent event);
	*/
	public String getPumaServerName();
}
