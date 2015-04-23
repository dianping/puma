package com.dianping.puma.monitor;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.core.monitor.EventMonitor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("storageEventCountMonitor")
public class StorageEventCountMonitor {

	private static final String MONITOR_TITLE = "EventCount.storage";

	private static final String STORAGE_EVENT_COUNT_INTERNAL = "puma.server.eventcount.storage.internal";

	private EventMonitor eventMonitor;

	private ConfigCache configCache;

	private long storageEventCountInternal;

	public StorageEventCountMonitor() {
		eventMonitor = new EventMonitor();
		configCache = ConfigCache.getInstance();
		storageEventCountInternal = 1000; // Default.
	}

	@PostConstruct
	public void init() {
		storageEventCountInternal = configCache.getLongProperty(STORAGE_EVENT_COUNT_INTERNAL);
		eventMonitor.setType(MONITOR_TITLE);
		eventMonitor.setCountThreshold(storageEventCountInternal);
		eventMonitor.start();

		configCache.addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (key.equals(STORAGE_EVENT_COUNT_INTERNAL)) {
					storageEventCountInternal = Long.parseLong(value);
					eventMonitor.stop();
					eventMonitor.setCountThreshold(storageEventCountInternal);
					eventMonitor.start();
				}
			}
		});
	}

	public void record(String taskName) {
		if (eventMonitor != null) {
			eventMonitor.record(taskName, "0");
		}
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}

	public void setEventMonitor(EventMonitor eventMonitor) {
		this.eventMonitor = eventMonitor;
	}
}
