package com.dianping.puma.core.monitor;

public interface MonitorCore {

	void put(Object key, Object value);

	void remove(Object key);

	void clear();

	void log();
}
