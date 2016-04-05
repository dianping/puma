package com.dianping.puma.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class PropertiesConfigManager implements ConfigManager {

    private String propertiesFilePath;

    private Properties properties;

    public void init() {
        try {
            InputStream inputStream = PropertiesConfigManager.class.getResourceAsStream(propertiesFilePath);
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to init properties config manager.", e);
        }
    }

    @Override
    public void createConfig(String project, String key, String desc) {

    }

    @Override
    public void setConfig(String key, String value) {

    }

    @Override
    public String getConfig(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Map<String, String> getConfigByProject(String env, String project) {
        return null;
    }

    @Override
    public void addConfigChangeListener(String key, ConfigChangeListener listener) {

    }

    @Override
    public void removeConfigChangeListener(String key, ConfigChangeListener listener) {

    }

    public void setPropertiesFilePath(String propertiesFilePath) {
        this.propertiesFilePath = propertiesFilePath;
    }
}
