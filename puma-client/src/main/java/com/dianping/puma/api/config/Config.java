package com.dianping.puma.api.config;

import com.dianping.cat.Cat;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.exception.PumaException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Config {

	private static final Logger logger = LoggerFactory.getLogger(Config.class);

	private static final String TARGET_KEY = "puma.client.target";
	private static final String SERVER_ID_KEY = "puma.client.serverid";
	private static final String DML_KEY = "puma.client.dml";
	private static final String DDL_KEY = "puma.client.ddl";
	private static final String TRANSACTION_KEY = "puma.client.transaction";
	private static final String CODEC_TYPE_KEY = "puma.client.codectype";
	private static final String SCHEMA_KEY = "puma.client.schema";
	private static final String TABLES_KEY = "puma.client.tables";

	private static final String RECONNECT_SLEEP_TIME_KEY = "puma.client.reconnect.sleep.time";
	private static final String RECONNECT_COUNT_KEY = "puma.client.reconnect.count";
	private static final String HEARTBEAT_CHECK_TIME_KEY = "puma.client.heartbeat.check.time";
	private static final String HEARTBEAT_EXPIRED_TIME_KEY = "puma.client.heartbeat.expired.time";
	private static final String BINLOG_ACK_TIME_KEY = "puma.client.binlog.ack.time";
	private static final String BINLOG_ACK_COUNT_KEY = "puma.client.binlog.ack.count";
	private static final String BINLOG_EXPIRED_TIME_KEY = "puma.client.binlog.expired.time";

	private boolean inited = false;

	private volatile String target;                      // prerequisite.
	private volatile Long serverId;                      // prerequisite.
	private volatile String schema;                      // prerequisite.
	private volatile List<String> tables;                // prerequisite.
	private volatile Boolean dml = true;                 // optional.
	private volatile Boolean ddl = false;                // optional.
	private volatile Boolean transaction = false;        // optional.
	private volatile String codecType = "json";          // optional.


	private volatile Long reconnectSleepTime = 3000L;    // optional.
	private volatile Integer reconnectCount = 3;         // optional.
	private volatile Long heartbeatCheckTime = 18000L;   // optional.
	private volatile Long heartbeatExpiredTime = 18000L; // optional.
	private volatile Long binlogAckTime = 1000L;         // optional.
	private volatile Integer binlogAckCount = 100;       // optional.
	private volatile Long binlogExpiredTime = 18000L;    // optional.

	private PumaClient client;
	private ConfigCache configCache;

	private ConfigChange configChange = new ConfigChange() {
		@Override
		public void onChange(String key, String value) {
			try {

				// local configurations.
				if (key.equalsIgnoreCase(localKey(TARGET_KEY))) {
					target = (String) genConfig(target, value, true);
				} else if (key.equalsIgnoreCase(localKey(SERVER_ID_KEY))) {
					serverId = (Long) genConfig(serverId, Long.parseLong(value), true);
				} else if (key.equalsIgnoreCase(localKey(SCHEMA_KEY))) {
					schema = (String) genConfig(schema, value, true);
				} else if (key.equalsIgnoreCase(localKey(TABLES_KEY))) {
					tables = (List<String>) genConfig(tables, parseTables(value), true);
				} else if (key.equalsIgnoreCase(localKey(DML_KEY))) {
					dml = (Boolean) genConfig(dml, Boolean.parseBoolean(value), false);
				} else if (key.equalsIgnoreCase(localKey(DDL_KEY))) {
					ddl = (Boolean) genConfig(ddl, Boolean.parseBoolean(value), false);
				} else if (key.equalsIgnoreCase(localKey(TRANSACTION_KEY))) {
					transaction = (Boolean) genConfig(transaction, Boolean.parseBoolean(value), false);
				} else if (key.equalsIgnoreCase(localKey(CODEC_TYPE_KEY))) {
					codecType = (String) genConfig(codecType, value, false);
				}

				// global configurations.
				if (key.equalsIgnoreCase(globalKey(RECONNECT_SLEEP_TIME_KEY))) {
					reconnectSleepTime = (Long) genConfig(reconnectSleepTime, Long.parseLong(value), false);
				} else if (key.equalsIgnoreCase(globalKey(RECONNECT_COUNT_KEY))) {
					reconnectCount = (Integer) genConfig(reconnectCount, Integer.parseInt(value), false);
				} else if (key.equalsIgnoreCase(globalKey(HEARTBEAT_CHECK_TIME_KEY))) {
					heartbeatCheckTime = (Long) genConfig(heartbeatCheckTime, Long.parseLong(value), false);
				} else if (key.equalsIgnoreCase(globalKey(HEARTBEAT_EXPIRED_TIME_KEY))) {
					heartbeatExpiredTime = (Long) genConfig(heartbeatExpiredTime, Long.parseLong(value), false);
				} else if (key.equalsIgnoreCase(globalKey(BINLOG_ACK_TIME_KEY))) {
					binlogAckTime = (Long) genConfig(binlogAckTime, Long.parseLong(value), false);
				} else if (key.equalsIgnoreCase(globalKey(BINLOG_ACK_COUNT_KEY))) {
					binlogAckCount = (Integer) genConfig(BINLOG_ACK_COUNT_KEY, Integer.parseInt(value), false);
				} else if (key.equalsIgnoreCase(globalKey(BINLOG_EXPIRED_TIME_KEY))) {
					binlogExpiredTime = (Long) genConfig(BINLOG_EXPIRED_TIME_KEY, Long.parseLong(value), false);
				}

			} catch (Exception e) {
				String msg = String.format("Puma changing configuration error.");
				PumaException pe = new PumaException(client.getName(), msg);
				logger.error(msg, pe);
				Cat.logError(msg, pe);
			}
		}
	};

	public void start() {
		if (inited) {
			logger.warn("Puma client(%s) local config is already started.", client.getName());
			return;
		}

		// Set local configurations.
		target = (String) genConfig(target, configCache.getProperty(localKey(TARGET_KEY)), true);
		serverId = (Long) genConfig(serverId, configCache.getLongProperty(localKey(SERVER_ID_KEY)), true);
		schema = (String) genConfig(schema, configCache.getProperty(SCHEMA_KEY), true);
		tables = (List<String>) genConfig(tables, parseTables(configCache.getProperty(TABLES_KEY)), true);
		dml = (Boolean) genConfig(dml, configCache.getBooleanProperty(localKey(DML_KEY)), false);
		ddl = (Boolean) genConfig(ddl, configCache.getBooleanProperty(localKey(DDL_KEY)), false);
		transaction = (Boolean) genConfig(transaction, configCache.getBooleanProperty(localKey(TRANSACTION_KEY)), false);
		codecType = (String) genConfig(codecType, configCache.getProperty(CODEC_TYPE_KEY), false);

		// Set global configurations.
		reconnectSleepTime = (Long) genConfig(reconnectSleepTime, configCache.getLongProperty(RECONNECT_SLEEP_TIME_KEY), false);
		reconnectCount = (Integer) genConfig(reconnectCount, configCache.getIntProperty(RECONNECT_COUNT_KEY), false);
		heartbeatCheckTime = (Long) genConfig(heartbeatCheckTime, configCache.getLongProperty(HEARTBEAT_CHECK_TIME_KEY), false);
		heartbeatExpiredTime = (Long) genConfig(heartbeatExpiredTime, configCache.getLongProperty(HEARTBEAT_EXPIRED_TIME_KEY), false);
		binlogAckTime = (Long) genConfig(binlogAckTime, configCache.getLongProperty(BINLOG_ACK_TIME_KEY), false);
		binlogAckCount = (Integer) genConfig(binlogAckCount, configCache.getIntProperty(BINLOG_ACK_COUNT_KEY), false);
		binlogExpiredTime = (Long) genConfig(binlogExpiredTime, configCache.getLongProperty(BINLOG_EXPIRED_TIME_KEY), false);

		// Register listener.
		configCache.addChange(configChange);

		inited = true;
	}

	public void stop() {
		if (!inited) {
			logger.warn("Puma client(%s) local config is already stopped.", client.getName());
			return;
		}

		// Unregister listener.
		configCache.removeChange(configChange);

		inited = false;
	}

	private List<String> parseTables(String tableString) {
		return Arrays.asList(StringUtils.split(tableString, ","));
	}

	private Object genConfig(Object oldValue, Object newValue, boolean optional) {
		if (newValue == null) {
			if (optional) {
				return oldValue;
			} else {
				throw new NullPointerException("Prerequisite configuration is null.");
			}
		} else {
			return newValue;
		}
	}

	private String localKey(String key) {
		return (new StringBuilder())
				.append(key)
				.append(".")
				.append(client.getName())
				.toString();
	}

	private String globalKey(String key) {
		return key;
	}

	public Long getServerId() {
		return serverId;
	}

	public String getTarget() {
		return target;
	}

	public Boolean getDml() {
		return dml;
	}

	public Boolean getDdl() {
		return ddl;
	}

	public Boolean getTransaction() {
		return transaction;
	}

	public String getCodecType() {
		return codecType;
	}

	public Long getReconnectSleepTime() {
		return reconnectSleepTime;
	}

	public Integer getReconnectCount() {
		return reconnectCount;
	}

	public Long getHeartbeatCheckTime() {
		return heartbeatCheckTime;
	}

	public Long getHeartbeatExpiredTime() {
		return heartbeatExpiredTime;
	}

	public Long getBinlogAckTime() {
		return binlogAckTime;
	}

	public Integer getBinlogAckCount() {
		return binlogAckCount;
	}

	public Long getBinlogExpiredTime() {
		return binlogExpiredTime;
	}

	public void setClient(PumaClient client) {
		this.client = client;
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}
}
