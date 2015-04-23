package com.dianping.puma.monitor.parser;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.core.monitor.EventMonitor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("parserEventCountMonitor")
public class ParserEventCountMonitor {

	private static final String MONITOR_TITLE = "EventCount.parser";

	private static final String PARSER_EVENT_COUNT_INTERNAL = "puma.server.eventcount.parser.internal";

	private EventMonitor eventMonitor;

	private ConfigCache configCache;

	private long parserEventCountInternal;

	public ParserEventCountMonitor() {
		eventMonitor = new EventMonitor();
		configCache = ConfigCache.getInstance();
		parserEventCountInternal = 10000; // Default.
	}

	@PostConstruct
	public void init() {
		parserEventCountInternal = configCache.getLongProperty(PARSER_EVENT_COUNT_INTERNAL);
		eventMonitor.setType(MONITOR_TITLE);
		eventMonitor.setCountThreshold(parserEventCountInternal);
		eventMonitor.start();

		configCache.addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (key.equals(PARSER_EVENT_COUNT_INTERNAL)) {
					parserEventCountInternal = Long.parseLong(value);
					eventMonitor.stop();
					eventMonitor.setCountThreshold(parserEventCountInternal);
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
