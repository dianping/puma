package com.dianping.puma.monitor;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.core.monitor.HeartbeatMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("serverEventDelayMonitor")
public class ServerEventDelayMonitor {

	private static final Logger LOG = LoggerFactory.getLogger(ServerEventDelayMonitor.class);

	private static final String MONITOR_TITLE = "EventDelay.server";

	private static final String SERVER_EVENT_DELAY_THRESHOLD = "puma.server.eventdelay.server.threshold";

	private long serverEventDelayThreshold;

	private HeartbeatMonitor heartbeatMonitor;

	private ConfigCache configCache;

	private int delay;

	private int period;

	public ServerEventDelayMonitor() {
		heartbeatMonitor = new HeartbeatMonitor();
		configCache = ConfigCache.getInstance();
		delay = 60; // 60s.
		period = 60; // 60s.
	}

	@PostConstruct
	public void init() {
		serverEventDelayThreshold = ConfigCache.getInstance().getLongProperty(SERVER_EVENT_DELAY_THRESHOLD);
		heartbeatMonitor.setType(MONITOR_TITLE);
		heartbeatMonitor.setDelaySeconds(delay);
		heartbeatMonitor.setPeriodSeconds(period);
		heartbeatMonitor.start();

		configCache.addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (key.equals(SERVER_EVENT_DELAY_THRESHOLD)) {
					serverEventDelayThreshold = Long.parseLong(value);
				}
			}
		});
	}

	public void record(String taskName, long execTime) {
		heartbeatMonitor.record(taskName, genStatus(execTime));
	}

	private String genStatus(long execSeconds) {
		long diff = System.currentTimeMillis() / 1000 - execSeconds;
		return (diff < serverEventDelayThreshold) ? "0" : "1";
	}

	public void setHeartbeatMonitor(HeartbeatMonitor heartbeatMonitor) {
		this.heartbeatMonitor = heartbeatMonitor;
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}
}
