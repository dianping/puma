package com.dianping.puma.api;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;

@Deprecated
public class ConfigurationBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationBuilder.class);

	private Configuration configuration;

	private static final String SUFFIX_HOST = ".host";
	private static final String SUFFIX_PORT = ".port";
	private static final String SUFFIX_SERVERID = ".serverid";
	private static final String PREFIX_NAME = "puma.server.";

	private void initTargetConfig() {
		final String hostKey = PREFIX_NAME + StringUtils.lowerCase(this.configuration.getTarget()) + SUFFIX_HOST;
		final String portKey = PREFIX_NAME + StringUtils.lowerCase(this.configuration.getTarget()) + SUFFIX_PORT;
		final String serverIdKey = PREFIX_NAME + StringUtils.lowerCase(this.configuration.getTarget()) + SUFFIX_SERVERID;
		try {
			setLionConfigHost(hostKey);
			setLionConfigPort(portKey);
			setLionConfigServerId(serverIdKey);
			ConfigCache.getInstance().addChange(new ConfigChange() {
				@Override
				public void onChange(String key, String value) {
					if (key.equals(hostKey)) {
						setLionConfigHost(hostKey);
					} else if (key.equals(portKey)) {
						setLionConfigPort(portKey);
					} else if (key.equals(serverIdKey)) {
						setLionConfigServerId(serverIdKey);
					}
				}
			});
		} catch (LionException e) {
			LOG.info("Lion not exist target config. Reason: " + e.getMessage());
		}
	}

	private void setLionConfigHost(String key) {
		String host = ConfigCache.getInstance().getProperty(key);
		if (StringUtils.isNotBlank(host)) {
			this.configuration.setHost(host);
		}
	}

	private void setLionConfigPort(String key) {
		Integer port = ConfigCache.getInstance().getIntProperty(key);
		if (port != null) {
			this.configuration.setPort(port.intValue());
		}
	}

	private void setLionConfigServerId(String key) {
		Long serverId = ConfigCache.getInstance().getLongProperty(key);
		if (serverId != null) {
			this.configuration.setServerId(serverId.longValue());
		}
	}

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

	public ConfigurationBuilder serverId(long serverId) {
		this.configuration.setServerId(serverId);
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

	public ConfigurationBuilder timeStamp(long timeStamp) {
		this.configuration.setTimeStamp(timeStamp);
		return this;
	}

	public Configuration build() {
		initTargetConfig();
		return this.configuration;
	}

	public ConfigurationBuilder target(String target) {
		this.configuration.setTarget(target);
		return this;
	}

}
