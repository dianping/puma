package com.dianping.puma.portal.config;

public class ConfigManagerLoader {

	private static final ConfigManager configManager = new LionConfigManager();

	public static ConfigManager getConfigManager() {
		return configManager;
	}
}
