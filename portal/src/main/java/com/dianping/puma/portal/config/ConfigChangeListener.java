package com.dianping.puma.portal.config;

public interface ConfigChangeListener {

	void onConfigChange(String oldValue, String newValue);
}
