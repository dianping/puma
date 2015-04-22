package com.dianping.puma.monitor.fetcher;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.core.monitor.EventMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

public class FetcherEventCountMonitor {

	private static final Logger LOG = LoggerFactory.getLogger(FetcherEventCountMonitor.class);

	private static final String MONITOR_TITLE = "EventCount.fetcher";

	private static final String FETCHER_EVENT_COUNT_INTERNAL = "puma.server.eventcount.fetcher.internal";

	private long fetcherEventCountInternal;

	private EventMonitor eventMonitor;

	public FetcherEventCountMonitor() {}

	@PostConstruct
	public void init() {
		fetcherEventCountInternal = ConfigCache.getInstance().getLongProperty(FETCHER_EVENT_COUNT_INTERNAL);

		ConfigCache.getInstance().addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (key.equals(FETCHER_EVENT_COUNT_INTERNAL)) {
					fetcherEventCountInternal = Long.parseLong(value);
					stop();
					initMonitor();
					start();
				}
			}
		});

		initMonitor();
		start();
	}

	public void record(String taskName) {
		if (eventMonitor != null && !eventMonitor.isStopped()) {
			eventMonitor.record(taskName, "0");
		}
	}

	private void initMonitor() {
		if (eventMonitor != null && !eventMonitor.isStopped()) {
			eventMonitor.stop();
		}
		LOG.info("Initialize fetcher event count monitor.");
		eventMonitor = new EventMonitor(MONITOR_TITLE, fetcherEventCountInternal);
	}

	private void start() {
		if (eventMonitor != null && eventMonitor.isStopped()) {
			LOG.info("Start fetcher event count monitor.");
			eventMonitor.start();
		}
	}

	private void stop() {
		if (eventMonitor != null && !eventMonitor.isStopped()) {
			LOG.info("Stop fetcher event count monitor.");
			eventMonitor.stop();
		}
	}
}
