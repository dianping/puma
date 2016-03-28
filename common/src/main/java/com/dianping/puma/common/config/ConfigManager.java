package com.dianping.puma.common.config;

import java.util.Map;

public interface ConfigManager {

    void createConfig(String project, String key, String desc);

    void setConfig(String key, String value);

    String getConfig(String key);

    Map<String, String> getConfigByProject(String env, String project);

    void addConfigChangeListener(String key, ConfigChangeListener listener);

    void removeConfigChangeListener(String key, ConfigChangeListener listener);
}
