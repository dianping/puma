package com.dianping.puma.core.config;

public interface ConfigChangeListener {

	public void onConfigChange(String oldValue, String newValue);
}
