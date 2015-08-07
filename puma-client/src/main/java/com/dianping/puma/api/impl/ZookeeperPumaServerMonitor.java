package com.dianping.puma.api.impl;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.api.PumaServerMonitor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZookeeperPumaServerMonitor implements PumaServerMonitor {

	private static final String ZK_BASE_PATH = "puma.client.route.";

	protected final ConfigCache cc = ConfigCache.getInstance();

	protected ConcurrentMap<String, ConfigChange> configChanges = new ConcurrentHashMap<String, ConfigChange>();

	@Override
	public List<String> fetch(String database, List<String> tables) {
		String zkPath = buildZkPath(database, tables);
		String zkNode = cc.getProperty(zkPath);
		return parseServers(zkNode);
	}

	@Override
	public void addListener(final String database, final List<String> tables, final PumaServerMonitorListener listener) {
		ConfigChange configChange = new ConfigChange() {
			@Override
			public void onChange(String zkPath, String zkNode) {
				if (zkPath.equalsIgnoreCase(buildZkPath(database, tables))) {
					listener.onChange(parseServers(zkNode));
				}
			}
		};

		cc.addChange(configChange);
		configChanges.put(database, configChange);
	}

	@Override
	public void removeListener(final String database, final List<String> tables) {
		ConfigChange configChange = configChanges.get(database);
		if (configChange != null) {
			cc.removeChange(configChange);
			configChanges.remove(database);
		}
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
