package com.dianping.puma.checkserver.manager.server;

import java.util.Collection;

public interface CheckTaskServerManager {

	public String findFirstAuthorizedHost();

	public Collection<String> findAuthorizedHosts();
}
