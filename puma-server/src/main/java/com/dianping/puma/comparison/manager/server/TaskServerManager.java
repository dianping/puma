package com.dianping.puma.comparison.manager.server;

import java.util.Collection;

public interface TaskServerManager {

	public String findFirstAuthorizedHost();

	public Collection<String> findAuthorizedHosts();
}
