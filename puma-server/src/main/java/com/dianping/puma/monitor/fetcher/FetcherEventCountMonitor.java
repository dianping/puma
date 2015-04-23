package com.dianping.puma.monitor.fetcher;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.core.monitor.EventMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("fetcherEventCountMonitor")
public class FetcherEventCountMonitor {

	private static final Logger LOG = LoggerFactory.getLogger(FetcherEventCountMonitor.class);

	private static final String MONITOR_TITLE = "EventCount.fetcher";

	private static final String FETCHER_EVENT_COUNT_INTERNAL = "puma.server.eventcount.fetcher.internal";

	private EventMonitor eventMonitor = new EventMonitor();

	private ConfigCache configCache = ConfigCache.getInstance();

	private long fetcherEventCountInternal;

	public FetcherEventCountMonitor() {}

	@PostConstruct
	public void init() {
		fetcherEventCountInternal = configCache.getLongProperty(FETCHER_EVENT_COUNT_INTERNAL);
		initMonitor(MONITOR_TITLE, fetcherEventCountInternal);

		// Listen to lion changes.
		configCache.addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (key.equals(FETCHER_EVENT_COUNT_INTERNAL)) {
					fetcherEventCountInternal = Long.parseLong(value);
					initMonitor(MONITOR_TITLE, fetcherEventCountInternal);
				}
			}
		});
	}

	public void record(String taskName) {
		if (eventMonitor != null) {
			eventMonitor.record(taskName, "0");
		}
	}

	public void setEventMonitor(EventMonitor eventMonitor) {
		this.eventMonitor = eventMonitor;
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}

	private void initMonitor(String type, long count) {
		eventMonitor.stop();
		eventMonitor.setType(type);
		eventMonitor.setCountThreshold(count);
		eventMonitor.start();
	}
}
