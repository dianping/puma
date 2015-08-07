package com.dianping.puma.core.config;

public class ConfigManagerLoader {

	private static final ConfigManager configManager = new LionConfigManager();

	public ConfigManager getConfigManager() {
		return configManager;
	}
}
