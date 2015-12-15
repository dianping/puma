package com.dianping.puma.config;

public interface ConfigChangeListener {

	void onConfigChange(String oldValue, String newValue);
}
