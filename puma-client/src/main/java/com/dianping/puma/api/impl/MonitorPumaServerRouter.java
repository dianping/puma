package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaServerMonitor;
import com.dianping.puma.api.PumaServerRouter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorPumaServerRouter implements PumaServerRouter {

	private final long frozenDuration = 60 * 1000; // 60s.

	protected Map<String, Long> frozenTimes = new HashMap<String, Long>();

	protected PumaServerMonitor monitor;

	private String database;

	private List<String> tables;

	public MonitorPumaServerRouter() {}

	public MonitorPumaServerRouter(PumaServerMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public String next() {
		List<String> servers = monitor.fetch(database, tables);

		if (servers == null || servers.size() == 0) {
			throw new RuntimeException("failed to route, no puma server available.");
		}

		for (String server: servers) {
			Long now = System.currentTimeMillis();
			Long frozenTime = frozenTimes.get(server);

			if (frozenTime == null || isFrozenExpired(now, frozenTime)) {
				frozenTimes.put(server, now);
				return server;
			}
		}

		throw new RuntimeException("failed to route, all puma servers are failed recently.");
	}

	protected boolean isFrozenExpired(long now, long frozenTime) {
		return (now - frozenTime) >= frozenDuration;
	}
}
