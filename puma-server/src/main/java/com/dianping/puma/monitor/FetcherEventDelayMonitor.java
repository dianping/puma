package com.dianping.puma.monitor;

import com.dianping.cat.Cat;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.core.monitor.HeartbeatMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("fetcherEventDelayMonitor")
public class FetcherEventDelayMonitor {

	private static final Logger LOG = LoggerFactory.getLogger(FetcherEventDelayMonitor.class);

	private static final String MONITOR_TITLE = "EventDelay.fetcher";

	private static final String FETCHER_EVENT_DELAY_THRESHOLD = "puma.server.eventdelay.fetcher.threshold";

	private long fetcherEventDelayThreshold;

	private HeartbeatMonitor heartbeatMonitor;

	private ConfigCache configCache;

	private int delay;

	private int period;

	public FetcherEventDelayMonitor() {
		heartbeatMonitor = new HeartbeatMonitor();
		configCache = ConfigCache.getInstance();
		delay = 60; // 60s.
		period = 60; // 60s.
	}

	@PostConstruct
	public void init() {
		fetcherEventDelayThreshold = configCache.getLongProperty(FETCHER_EVENT_DELAY_THRESHOLD);
		heartbeatMonitor.setType(MONITOR_TITLE);
		heartbeatMonitor.setPeriodSeconds(period);
		heartbeatMonitor.start();

		configCache.addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (key.equals(FETCHER_EVENT_DELAY_THRESHOLD)) {
					fetcherEventDelayThreshold = Long.parseLong(value);
				}
			}
		});
	}

	public void record(String taskName, long execTime) {
		heartbeatMonitor.record(taskName, genStatus(execTime));
	}

	private String genStatus(long execSeconds) {
		long diff = System.currentTimeMillis() / 1000 - execSeconds;
		return diff < fetcherEventDelayThreshold ? "0" : "1";
	}

	public void setHeartbeatMonitor(HeartbeatMonitor heartbeatMonitor) {
		this.heartbeatMonitor = heartbeatMonitor;
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}
}
