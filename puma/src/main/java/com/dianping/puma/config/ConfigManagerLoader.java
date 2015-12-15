package com.dianping.puma.config;

public class ConfigManagerLoader {

	private static final ConfigManager configManager = new LionConfigManager();

	public static ConfigManager getConfigManager() {
		return configManager;
	}
}
