package com.dianping.puma.api;

import java.util.List;

public interface PumaServerMonitor {

	public List<String> get();

	public void addListener(PumaServerMonitorListener listener);

	public void removeListener();

	public interface PumaServerMonitorListener {
		public void onChange(List<String> oriServers, List<String> servers);
	}
}
