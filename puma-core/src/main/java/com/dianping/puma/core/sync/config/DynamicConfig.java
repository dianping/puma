package com.dianping.puma.core.sync.config;

public interface DynamicConfig {

   String get(String key);

   void setConfigChangeListener(ConfigChangeListener listener);
}
