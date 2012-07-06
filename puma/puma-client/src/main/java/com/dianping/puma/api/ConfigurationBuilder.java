package com.dianping.puma.api;

public class ConfigurationBuilder {

	private Configuration	configuration;

	public ConfigurationBuilder() {
		this.configuration = new Configuration();
	}

	public ConfigurationBuilder host(String host) {
		this.configuration.setHost(host);
		return this;
	}

	public ConfigurationBuilder port(int port) {
		this.configuration.setPort(port);
		return this;
	}

	public ConfigurationBuilder tables(String database, String... tablePatterns) {
		this.configuration.addDatabaseTable(database, tablePatterns);
		return this;
	}

	public ConfigurationBuilder ddl(boolean needDdl) {
		this.configuration.setNeedDdl(needDdl);
		return this;
	}

	public ConfigurationBuilder dml(boolean needDml) {
		this.configuration.setNeedDml(needDml);
		return this;
	}

	public ConfigurationBuilder transaction(boolean needTransactionInfo) {
		this.configuration.setNeedTransactionInfo(needTransactionInfo);
		return this;
	}

	public ConfigurationBuilder codecType(String codecType) {
		this.configuration.setCodecType(codecType);
		return this;
	}

	public Configuration build() {
		return this.configuration;
	}
}
