package com.dianping.puma.server;

import java.util.concurrent.ConcurrentHashMap;

import com.dianping.puma.core.monitor.ReplicationTaskEvent;
import com.dianping.puma.core.monitor.ReplicationTaskStatusEvent;
import com.dianping.puma.core.replicate.model.task.ReplicationTask;
import com.dianping.puma.storage.EventStorage;

public interface TaskManager {

	public void init();

	public Server construct(ReplicationTask replicationTask)
			throws Exception;

	public ConcurrentHashMap<String,Server> constructServers() throws Exception;

	public void initContext(Server server);

	public boolean contain(String taskName);

	public boolean addServer(Server server);

	public void remove(String taskName);

	public void startServer(final Server server);

	public void stopServer(Server server);
	
	public void stopServers();
	
	public void startEvent(ReplicationTaskStatusEvent event);
	
	public void stopEvent(ReplicationTaskStatusEvent event);
	
	public void restartEvent(ReplicationTaskStatusEvent event);
	
	public void addEvent(ReplicationTaskEvent event);
	
	public void deleteEvent(ReplicationTaskEvent event);
	
	public void updateEvent(ReplicationTaskEvent event);
	
}
