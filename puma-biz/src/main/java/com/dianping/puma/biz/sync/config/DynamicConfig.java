package com.dianping.puma.biz.sync.config;

public interface DynamicConfig {

   String get(String key);

   void setConfigChangeListener(ConfigChangeListener listener);
}
