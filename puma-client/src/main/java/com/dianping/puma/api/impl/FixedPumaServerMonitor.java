package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaServerMonitor;

import java.util.List;

public class FixedPumaServerMonitor implements PumaServerMonitor {

	private List<String> servers;

	public FixedPumaServerMonitor(List<String> servers) {
		this.servers = servers;
	}

	@Override
	public List<String> get() {
		return servers;
	}

	@Override
	public void addListener(PumaServerMonitorListener listener) {

	}

	@Override
	public void removeListener() {

	}
}
