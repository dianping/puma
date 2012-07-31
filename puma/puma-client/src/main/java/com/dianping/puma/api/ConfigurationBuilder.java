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

	public ConfigurationBuilder name(String name) {
		this.configuration.setName(name);
		return this;
	}

	public ConfigurationBuilder seqFileBase(String seqFileBase) {
		this.configuration.setSeqFileBase(seqFileBase);
		return this;
	}

	public ConfigurationBuilder maxRetryTimes(int maxRetryTimes) {
		this.configuration.setMaxRetryTimes(maxRetryTimes);
		return this;
	}

	public ConfigurationBuilder exceptionHandler(ExceptionHandler exceptionHandler) {
		this.configuration.setExceptionHandler(exceptionHandler);
		return this;
	}

	public ConfigurationBuilder canSkip(boolean canSkip) {
		this.configuration.setCanSkip(canSkip);
		return this;
	}

	public ConfigurationBuilder masterUrl(String masterUrl) {
		this.configuration.setMasterUrl(masterUrl);
		return this;
	}

	public ConfigurationBuilder binlog(String binlog) {
		this.configuration.setBinlog(binlog);
		return this;
	}

	public ConfigurationBuilder binlogPos(long binlogPos) {
		this.configuration.setBinlogPos(binlogPos);
		return this;
	}

	public Configuration build() {
		return this.configuration;
	}

	public ConfigurationBuilder target(String target) {
		this.configuration.setTarget(target);
		return this;
	}
}
