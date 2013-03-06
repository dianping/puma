package com.dianping.puma.core.sync.config;

public interface ConfigChangeListener {

   void onConfigChange(String key, String value);

}
