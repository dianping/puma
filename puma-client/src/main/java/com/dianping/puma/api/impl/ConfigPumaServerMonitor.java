package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaServerMonitor;
import com.dianping.puma.core.config.ConfigChangeListener;
import com.dianping.puma.core.config.ConfigManager;
import com.dianping.puma.core.config.LionConfigManager;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ConfigPumaServerMonitor implements PumaServerMonitor {

	private static final String ZK_BASE_PATH = "puma.client.route.";

	protected ConfigManager configManager = new LionConfigManager();

	private final String zkPath;

	public ConfigPumaServerMonitor(String database, List<String> tables) {
		configManager = new LionConfigManager();
		zkPath = buildZkPath(database, tables);
	}

	@Override
	public List<String> get() {
		String zkNode = configManager.getConfig(zkPath);
		return parseServers(zkNode);
	}

	@Override
	public void addListener(final PumaServerMonitorListener listener) {
		ConfigChangeListener configChangeListener = new ConfigChangeListener() {
			@Override
			public void onConfigChange(String oldValue, String newValue) {
				listener.onChange(parseServers(oldValue), parseServers(newValue));
			}
		};

		configManager.addConfigChangeListener(zkPath, configChangeListener);
	}

	@Override
	public void removeListener() {
		configManager.removeConfigChangeListener(zkPath);
	}

	protected String buildZkPath(String database, List<String> tables) {
		return ZK_BASE_PATH + database;
	}

	protected List<String> parseServers(String zkNode) {
		List<String> servers = new ArrayList<String>();

		if (zkNode == null) {
			return servers;
		}

		String[] serverStrings = StringUtils.split(zkNode);
		if (serverStrings == null) {
			return servers;
		}

		for (int i = 0; i != serverStrings.length; ++i) {
			servers.add(StringUtils.normalizeSpace(serverStrings[i]));
		}
		return servers;
	}
}
