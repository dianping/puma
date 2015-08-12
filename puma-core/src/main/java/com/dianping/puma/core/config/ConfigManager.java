package com.dianping.puma.core.config;

public interface ConfigManager {

	public void createConfig(String project, String key, String desc);

	public void setConfig(String key, String value);

	public String getConfig(String key);

	public void addConfigChangeListener(String key, ConfigChangeListener listener);

	public void removeConfigChangeListener(String key, ConfigChangeListener listener);
}
