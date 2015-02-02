package com.dianping.puma.server;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.puma.core.monitor.ServerTaskActionEvent;
import com.dianping.puma.core.server.model.ServerTask;

public interface ServerManager {

	public void init();

	public Server construct(ServerTask config)
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
	
	public void startEvent(ServerTaskActionEvent event);
	
	public void stopEvent(ServerTaskActionEvent event);
	
	public void restartEvent(ServerTaskActionEvent event);
	
	public void addEvent(ServerTaskActionEvent event);
	
	public void deleteEvent(ServerTaskActionEvent event);
	
	public void updateEvent(ServerTaskActionEvent event);
}
