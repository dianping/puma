package com.dianping.puma.common.config;

import com.dianping.puma.common.extension.ExtensionLoader;

/**
 * Created by xiaotian.li on 16/3/27.
 * Email: lixiaotian07@gmail.com
 */
public class ConfigManagerLoader {

    private static final ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

    public static ConfigManager getConfigManager() {
        return configManager;
    }
}
