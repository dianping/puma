package com.dianping.puma.web.config;

public interface ConfigChangeListener {

	void onConfigChange(String oldValue, String newValue);
}
