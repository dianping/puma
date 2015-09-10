package com.dianping.puma.comparison.manager.server;

import java.util.Collection;

public interface CheckTaskServerManager {

	public String findFirstAuthorizedHost();

	public Collection<String> findAuthorizedHosts();
}
