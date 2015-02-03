package com.dianping.puma.server;

import java.util.concurrent.ConcurrentHashMap;

import com.dianping.puma.core.monitor.ReplicationTaskEvent;
import com.dianping.puma.core.monitor.ReplicationTaskStatusEvent;
import com.dianping.puma.core.replicate.model.task.ReplicationTask;

public interface TaskManager {

	public void init();

	public Server construct(ReplicationTask replicationTask)
			throws Exception;

	public ConcurrentHashMap<Long,Server> constructServers() throws Exception;

	public void initContext(Server server);

	public boolean contain(Long taskId);

	public boolean addServer(Server server);

	public void remove(Long taskId);

	public void startServer(final Server server);

	public void stopServer(Server server);
	
	public void stopServers();
	
	public ConcurrentHashMap<Long,Server> getServers();
	
	public void startEvent(ReplicationTaskStatusEvent event);
	
	public void stopEvent(ReplicationTaskStatusEvent event);
	
	public void restartEvent(ReplicationTaskStatusEvent event);
	
	public void addEvent(ReplicationTaskEvent event);
	
	public void deleteEvent(ReplicationTaskEvent event);
	
	public void updateEvent(ReplicationTaskEvent event);
}
