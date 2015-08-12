package com.dianping.puma.core.config;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LionConfigManager implements ConfigManager {

	protected final ConfigCache cc = ConfigCache.getInstance();

	protected volatile List<String> keys = new ArrayList<String>();

	protected ConcurrentMap<String, String> cache = new ConcurrentHashMap<String, String>();

	protected ConfigChange configChange;

	protected ConcurrentMap<String, ConfigChangeListener> listeners = new ConcurrentHashMap<String, ConfigChangeListener>();

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
		keys.add(key);
		listeners.put(key, listener);

		if (configChange == null) {
			configChange = new ConfigChange() {
				@Override
				public void onChange(String k, String v) {
					if (keys.contains(k)) {
						ConfigChangeListener configChangeListener = listeners.get(k);
						String oldValue = cache.get(k);
						cache.put(k, v);
						configChangeListener.onConfigChange(oldValue, v);
					}
				}
			};
			cc.addChange(configChange);
		}
	}

	@Override
	public void removeConfigChangeListener(String key, ConfigChangeListener listener) {
		keys.remove(key);
		listeners.remove(key, listener);
	}
}
