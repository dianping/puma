package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaServerMonitor;
import com.dianping.puma.api.PumaServerRouter;

import java.util.List;

public class RoundRobinPumaServerRouter implements PumaServerRouter {

	protected PumaServerMonitor monitor;

	private int index;

	public RoundRobinPumaServerRouter(PumaServerMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public String next() {
		List<String> servers = monitor.get();

		if (servers == null || servers.size() == 0) {
			return null;
		}

		if (index >= servers.size()) {
			index = index - servers.size();
		}

		return servers.get(index++);
	}
}
