package com.dianping.puma.biz.sync.config;

public interface ConfigChangeListener {

   void onConfigChange(String key, String value);

}
