package com.dianping.puma.core.config;

public interface ConfigManager {

	public String getConfig(String key);

	public void addConfigChangeListener(String key, ConfigChangeListener listener);

	public void removeConfigChangeListener(String key);
}
