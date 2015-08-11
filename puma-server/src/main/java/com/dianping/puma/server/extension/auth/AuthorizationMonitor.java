package com.dianping.puma.server.extension.auth;

public interface AuthorizationMonitor {

	public Authorization get(String host);

	public void addListener(String host, AuthorizationMonitorListener listener);

	public void removeListener(String host, AuthorizationMonitorListener listener);
}
