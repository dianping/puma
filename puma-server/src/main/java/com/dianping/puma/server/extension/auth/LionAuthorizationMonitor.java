package com.dianping.puma.server.extension.auth;

import com.dianping.puma.core.config.ConfigChangeListener;
import com.dianping.puma.core.config.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LionAuthorizationMonitor implements AuthorizationMonitor {

	protected static final String BINLOG_USERNAME_KEY = "puma.server.binlog.username";
	protected static final String BINLOG_PASSWORD_KEY = "puma.server.binlog.password";

	protected final Authorization cache = new Authorization();

	protected final ConcurrentMap<String, AuthorizationMonitorListener> listeners
			= new ConcurrentHashMap<String, AuthorizationMonitorListener>();

	protected ConfigChangeListener usernameConfigChangeListener, passwordConfigChangeListener;

	@Autowired
	ConfigManager configManager;

	@Override
	public Authorization get(String host) {
		String username = configManager.getConfig(BINLOG_USERNAME_KEY);
		String password = configManager.getConfig(BINLOG_PASSWORD_KEY);

		synchronized (cache) {
			cache.setUsername(username);
			cache.setPassword(password);
		}

		return new Authorization(cache.getUsername(), cache.getPassword());
	}

	@Override
	public void addListener(final String host, final AuthorizationMonitorListener listener) {
		listeners.put(host, listener);

		if (usernameConfigChangeListener == null) {
			usernameConfigChangeListener = new ConfigChangeListener() {
				@Override
				public void onConfigChange(String oldUsername, String newUsername) {
					synchronized (cache) {
						cache.setUsername(newUsername);

						for (AuthorizationMonitorListener _listener: listeners.values()) {
							_listener.onChange(new Authorization(cache.getUsername(), cache.getPassword()));
						}
					}
				}
			};
			configManager.addConfigChangeListener(BINLOG_USERNAME_KEY, usernameConfigChangeListener);
		}

		if (passwordConfigChangeListener == null) {
			passwordConfigChangeListener = new ConfigChangeListener() {
				@Override
				public void onConfigChange(String oldPassword, String newPassword) {
					synchronized (cache) {
						cache.setUsername(newPassword);

						for (AuthorizationMonitorListener _listener: listeners.values()) {
							_listener.onChange(new Authorization(cache.getUsername(), cache.getPassword()));
						}
					}
				}
			};
			configManager.addConfigChangeListener(BINLOG_PASSWORD_KEY, passwordConfigChangeListener);
		}
	}

	@Override
	public void removeListener(String host, AuthorizationMonitorListener listener) {
		listeners.remove(host, listener);
	}
}
