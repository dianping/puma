package com.dianping.puma.common.config;

import com.dianping.puma.common.extension.ExtensionLoader;

/**
 * Created by xiaotian.li on 16/3/27.
 * Email: lixiaotian07@gmail.com
 */
public class ConfigManagerLoader {

    private static PropertiesConfigManager propertiesConfigManager;

    private static ConfigManager extensionConfigManager;

    public static ConfigManager getConfigManager(String propertiesFilePath) {
        ConfigManager configManager = getExtensionConfigManager();
        return configManager == null ? getPropertiesConfigManager(propertiesFilePath) : configManager;
    }

    public static ConfigManager getPropertiesConfigManager(String propertiesFilePath) {
        if (propertiesConfigManager == null) {
            propertiesConfigManager = new PropertiesConfigManager();
            propertiesConfigManager.setPropertiesFilePath(propertiesFilePath);
            propertiesConfigManager.init();
        }
        return propertiesConfigManager;
    }

    public static ConfigManager getExtensionConfigManager() {
        if (extensionConfigManager == null) {
            extensionConfigManager = ExtensionLoader.getExtension(ConfigManager.class);
        }
        return extensionConfigManager;
    }
}
