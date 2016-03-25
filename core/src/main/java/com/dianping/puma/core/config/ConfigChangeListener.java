package com.dianping.puma.core.config;

public interface ConfigChangeListener {

	void onConfigChange(String oldValue, String newValue);
}
