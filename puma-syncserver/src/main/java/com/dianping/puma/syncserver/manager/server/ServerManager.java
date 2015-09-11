package com.dianping.puma.syncserver.manager.server;

import java.util.Collection;

public interface ServerManager {

	public Collection<String> findAuthorizedHosts();

	public void register();
}
