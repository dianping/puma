package com.dianping.puma.monitor;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.core.monitor.EventMonitor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("storageEventGroupMonitor")
public class StorageEventGroupMonitor {

	private static final String MONITOR_TITLE = "EventGroup.storage";

	private static final String STORAGE_EVENT_GROUP_INTERNAL = "puma.server.eventgroup.storage.internal";

	private EventMonitor eventMonitor;

	private ConfigCache configCache;

	private long storageEventGroupInternal;

	public StorageEventGroupMonitor() {
		eventMonitor = new EventMonitor();
		configCache = ConfigCache.getInstance();
		storageEventGroupInternal = 1; // Default.
	}

	@PostConstruct
	public void init() {
		storageEventGroupInternal = configCache.getLongProperty(STORAGE_EVENT_GROUP_INTERNAL);
		eventMonitor.setType(MONITOR_TITLE);
		eventMonitor.setCountThreshold(storageEventGroupInternal);
		eventMonitor.start();

		configCache.addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (key.equals(STORAGE_EVENT_GROUP_INTERNAL)) {
					storageEventGroupInternal = Long.parseLong(value);
					eventMonitor.stop();
					eventMonitor.setCountThreshold(storageEventGroupInternal);
					eventMonitor.start();
				}
			}
		});
	}

	public void record(String name) {
		if (eventMonitor != null) {
			eventMonitor.record(name, "0");
		}
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}

	public void setEventMonitor(EventMonitor eventMonitor) {
		this.eventMonitor = eventMonitor;
	}
}
