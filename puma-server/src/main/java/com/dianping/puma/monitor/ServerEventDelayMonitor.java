package com.dianping.puma.monitor;

import com.dianping.cat.Cat;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.biz.monitor.HeartbeatMonitor;
import com.dianping.puma.biz.monitor.MonitorCore;
import com.dianping.puma.monitor.exception.ServerEventDelayException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServerEventDelayMonitor {

	private static final String SERVER_EVENT_DELAY_THRESHOLD = "puma.server.eventdelay.server.threshold";

	private long serverEventDelayThreshold;

	private int periodSecond;

	private HeartbeatMonitor heartbeatMonitor;

	private ConfigCache configCache;

	public ServerEventDelayMonitor() {
		periodSecond = 60;
	}

	public void init() {
		configCache = ConfigCache.getInstance();
		configCache.addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (key.equals(SERVER_EVENT_DELAY_THRESHOLD)) {
					serverEventDelayThreshold = Long.parseLong(value);
				}
			}
		});

		heartbeatMonitor = new HeartbeatMonitor();
		serverEventDelayThreshold = configCache.getLongProperty(SERVER_EVENT_DELAY_THRESHOLD);
		heartbeatMonitor.setPeriodSeconds(periodSecond);
		heartbeatMonitor.setCore(new ServerEventDelayMonitorCore());
		heartbeatMonitor.start();
	}

	public void destroy() {
		heartbeatMonitor.stop();
	}

	private class ServerEventDelayMonitorCore implements MonitorCore {

		private ConcurrentMap<String, Boolean> cores = new ConcurrentHashMap<String, Boolean>();

		@Override
		public void put(Object key, Object value) {
			cores.put((String) key, isDelay((Long) value));
		}

		@Override
		public void remove(Object key) {
			cores.remove(key);
		}

		@Override
		public void clear() {
			cores.clear();
		}

		@Override
		public void log() {
			for (Map.Entry<String, Boolean> entry: cores.entrySet()) {
				if (entry.getValue()) {
					String msg = String.format("Puma task(%s) sends event delay.", entry.getKey());
					Cat.logError(msg, new ServerEventDelayException(msg));
				}
			}
		}

		private boolean isDelay(long execSeconds) {
			return (System.currentTimeMillis() / 1000 - execSeconds) > serverEventDelayThreshold;
		}
	}

	public void record(String taskName, long execTime) {
		heartbeatMonitor.record(taskName, execTime);
	}
	
	public void remove(String taskName) {
		heartbeatMonitor.remove(taskName);
	}

	public void setHeartbeatMonitor(HeartbeatMonitor heartbeatMonitor) {
		this.heartbeatMonitor = heartbeatMonitor;
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}
}
