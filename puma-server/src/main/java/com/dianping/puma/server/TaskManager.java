package com.dianping.puma.server;

import java.util.concurrent.ConcurrentHashMap;

import com.dianping.puma.core.entity.PumaTaskEntity;
import com.dianping.puma.core.monitor.PumaTaskOperationEvent;
import com.dianping.puma.core.monitor.ReplicationTaskEvent;
import com.dianping.puma.core.monitor.ReplicationTaskStatusActionEvent;
import com.dianping.puma.core.replicate.model.task.ReplicationTask;

public interface TaskManager {

	public void init();

	public Server construct(PumaTaskEntity pumaTask) throws Exception;

	public ConcurrentHashMap<String,Server> constructServers() throws Exception;

	public void initContext(Server server);

	public boolean contain(String taskName);

	public boolean addServer(Server server);

	public void remove(String taskName);

	public void startServer(final Server server);

	public void stopServer(Server server);
	
	public void stopServers();
	
	//public ConcurrentHashMap<Long,Server> getServers();
	
	public void startEvent(ReplicationTaskStatusActionEvent event);

	public void stopEvent(ReplicationTaskStatusActionEvent event);
	
	public void restartEvent(ReplicationTaskStatusActionEvent event);

	public void createEvent(PumaTaskOperationEvent event);

	public void updateEvent(PumaTaskOperationEvent event);

	public void removeEvent(PumaTaskOperationEvent event);

	/*
	public void addEvent(ReplicationTaskEvent event);
	
	public void deleteEvent(ReplicationTaskEvent event);
	
	public void updateEvent(ReplicationTaskEvent event);
	*/
	public String getServerName();
}
