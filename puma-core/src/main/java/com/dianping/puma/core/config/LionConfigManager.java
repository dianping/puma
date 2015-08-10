package com.dianping.puma.core.config;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LionConfigManager implements ConfigManager {

	protected ConfigCache cc = ConfigCache.getInstance();

	protected ConcurrentMap<String, String> cache = new ConcurrentHashMap<String, String>();

	protected ConcurrentMap<String, ConfigChange> configChanges = new ConcurrentHashMap<String, ConfigChange>();

	@Override
	public String getConfig(String key) {
		String value = cc.getProperty(key);
		if (value != null) {
			cache.put(key, value);
		}
		return value;
	}

	@Override
	public void addConfigChangeListener(final String key, final ConfigChangeListener listener) {
		ConfigChange configChange = new ConfigChange() {
			@Override
			public void onChange(String lionKey, String lionValue) {
				if (lionKey.equalsIgnoreCase(key)) {
					listener.onConfigChange(cache.get(key), lionValue);
				}
			}
		};

		configChanges.put(key, configChange);
		cc.addChange(configChange);
	}

	@Override
	public void removeConfigChangeListener(String key) {
		cc.removeChange(configChanges.get(key));
	}
}
