package com.dianping.puma.admin.config;

public interface DynamicConfig {

   String get(String key);

   void setConfigChangeListener(ConfigChangeListener listener);
}
