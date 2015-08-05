package com.dianping.puma.api;

import java.util.List;

public interface PumaServerMonitor {

	public List<String> fetch(String database, List<String> tables);

	public void addListener(String database, List<String> tables, PumaServerMonitorListener listener);

	public void removeListener(String database, List<String> tables);

	public interface PumaServerMonitorListener {
		public void onChange(List<String> servers);
	}
}
