package com.dianping.puma.api.impl;

import com.dianping.puma.api.PumaServerMonitor;

import java.util.List;

public class ConstantPumaServerMonitor implements PumaServerMonitor {

	private List<String> servers;

	public ConstantPumaServerMonitor(List<String> servers) {
		this.servers = servers;
	}

	@Override
	public List<String> fetch(String database, List<String> tables) {
		return servers;
	}

	@Override
	public void addListener(String database, List<String> tables, PumaServerMonitorListener listener) {

	}

	@Override
	public void removeListener(String database, List<String> tables) {

	}
}
