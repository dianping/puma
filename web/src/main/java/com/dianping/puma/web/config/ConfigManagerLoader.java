package com.dianping.puma.web.config;

public class ConfigManagerLoader {

	private static final ConfigManager configManager = new LionConfigManager();

	public static ConfigManager getConfigManager() {
		return configManager;
	}
}
