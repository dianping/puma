package com.dianping.puma.server.extension.auth;

import com.dianping.puma.core.config.ConfigChangeListener;
import com.dianping.puma.core.config.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LionAuthorizationMonitor implements AuthorizationMonitor {

	private static final String BINLOG_USERNAME_KEY = "puma.server.binlog.username";
	private static final String BINLOG_PASSWORD_KEY = "puma.server.binlog.password";

	protected ConcurrentMap<String, Authorization> authorizations
			= new ConcurrentHashMap<String, Authorization>();

	@Autowired
	ConfigManager configManager;

	@Override
	public Authorization get(String host) {
		String username = configManager.getConfig(BINLOG_USERNAME_KEY);
		String password = configManager.getConfig(BINLOG_PASSWORD_KEY);

		Authorization authorization = new Authorization();
		authorization.setUsername(username);
		authorization.setPassword(password);
		authorizations.put(host, authorization);

		return authorization;
	}

	@Override
	public void addListener(final String host, final AuthorizationMonitorListener listener) {
		ConfigChangeListener usernameConfigChangeListener = buildUsernameConfigChangeListener(host, listener);
		configManager.addConfigChangeListener(BINLOG_USERNAME_KEY, usernameConfigChangeListener);

		ConfigChangeListener passwordConfigChangeListener = buildPasswordConfigChangeListener(host, listener);
		configManager.addConfigChangeListener(BINLOG_PASSWORD_KEY, passwordConfigChangeListener);
	}

	@Override
	public void removeListener(String host, AuthorizationMonitorListener listener) {
		configManager.removeConfigChangeListener(
				BINLOG_USERNAME_KEY,
				buildUsernameConfigChangeListener(host, listener));

		configManager.removeConfigChangeListener(
				BINLOG_PASSWORD_KEY,
				buildPasswordConfigChangeListener(host, listener));
	}

	protected ConfigChangeListener buildUsernameConfigChangeListener(final String host, final AuthorizationMonitorListener listener) {
		return new ConfigChangeListener() {
			@Override
			public void onConfigChange(String oldUsername, String newUsername) {
				Authorization authorization = authorizations.get(host);
				authorization.setUsername(newUsername);
				listener.onChange(authorization);
			}
		};
	}

	protected ConfigChangeListener buildPasswordConfigChangeListener(final String host, final AuthorizationMonitorListener listener) {
		return new ConfigChangeListener() {
			@Override
			public void onConfigChange(String oldPassword, String newPassword) {
				Authorization authorization = authorizations.get(host);
				authorization.setUsername(newPassword);
				listener.onChange(authorization);
			}
		};
	}
}
