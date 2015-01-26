package com.dianping.puma.server;

import java.util.List;

import com.dianping.puma.core.server.model.PumaServerDetailConfig;

public interface ServerManager {

	public void init();

	public Server construct(PumaServerDetailConfig config)
			throws Exception;

	public List<Server> constructServers() throws Exception;

	public void initContext(Server server);

	public int indexOf(String serverName);

	public boolean contain(String serverName);

	public boolean add(Server server);

	public void remove(String serverName);

	public void start(final Server server);

	public void stop(Server server);
	
	public void stopServers();
	
	public List<Server> getServers();

}
