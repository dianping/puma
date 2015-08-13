package com.dianping.puma.core.config;

public class LionConfigManagerMainTest {

	public static void main(String[] args) {
		LionConfigManager lionConfigManager = new LionConfigManager();

		//lionConfigManager.createConfig("puma", "test-database", "hello");

		lionConfigManager.setConfig("test-database", "ok");

		String value = lionConfigManager.getConfig("test-database");
		System.out.println(value);
	}
}
