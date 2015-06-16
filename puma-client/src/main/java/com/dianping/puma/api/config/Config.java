package com.dianping.puma.api.config;

import com.dianping.cat.Cat;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.exception.PumaException;
import com.dianping.puma.api.util.Monitor;
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

	private static final String RECONNECT_SLEEP_TIME_KEY = "puma.client.reconnect.sleep.time";
	private static final String RECONNECT_COUNT_KEY = "puma.client.reconnect.count";
	private static final String HEARTBEAT_CHECK_TIME_KEY = "puma.client.heartbeat.check.time";
	private static final String HEARTBEAT_EXPIRED_TIME_KEY = "puma.client.heartbeat.expired.time";
	private static final String BINLOG_ACK_TIME_KEY = "puma.client.binlog.ack.time";
	private static final String BINLOG_ACK_COUNT_KEY = "puma.client.binlog.ack.count";
	private static final String ONEVENT_RETRY_COUNT_KEY = "puma.client.onevent.retry.count";

	private boolean inited = false;

	private volatile String target;                      // prerequisite.
	private volatile Long serverId;                      // prerequisite.
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
	private volatile Integer onEventRetryCount = 3;      // optional.

	private PumaClient client;
	private Monitor monitor;
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
				} else if (key.equalsIgnoreCase(globalKey(ONEVENT_RETRY_COUNT_KEY))) {
					onEventRetryCount = (Integer) genConfig(ONEVENT_RETRY_COUNT_KEY, Integer.parseInt(value), false);
				}

			} catch (Exception e) {
				monitor.logError(logger, "lion error", e);
			}
		}
	};

	public void start() {
		if (inited) {
			logger.warn("Puma client(%s) local config is already started.", client.getName());
			return;
		}

		// Set local configurations.
		target = (String) genConfig(target, configCache.getProperty(localKey(TARGET_KEY)), false);
		serverId = (Long) genConfig(serverId, configCache.getLongProperty(localKey(SERVER_ID_KEY)), false);
		dml = (Boolean) genConfig(dml, configCache.getBooleanProperty(localKey(DML_KEY)), true);
		ddl = (Boolean) genConfig(ddl, configCache.getBooleanProperty(localKey(DDL_KEY)), true);
		transaction = (Boolean) genConfig(transaction, configCache.getBooleanProperty(localKey(TRANSACTION_KEY)), true);
		codecType = (String) genConfig(codecType, configCache.getProperty(localKey(CODEC_TYPE_KEY)), true);

		// Set global configurations.
		reconnectSleepTime = (Long) genConfig(reconnectSleepTime, configCache.getLongProperty(globalKey(RECONNECT_SLEEP_TIME_KEY)), true);
		reconnectCount = (Integer) genConfig(reconnectCount, configCache.getIntProperty(globalKey(RECONNECT_COUNT_KEY)), true);
		heartbeatCheckTime = (Long) genConfig(heartbeatCheckTime, configCache.getLongProperty(globalKey(HEARTBEAT_CHECK_TIME_KEY)), true);
		heartbeatExpiredTime = (Long) genConfig(heartbeatExpiredTime, configCache.getLongProperty(globalKey(HEARTBEAT_EXPIRED_TIME_KEY)), true);
		binlogAckTime = (Long) genConfig(binlogAckTime, configCache.getLongProperty(globalKey(BINLOG_ACK_TIME_KEY)), true);
		binlogAckCount = (Integer) genConfig(binlogAckCount, configCache.getIntProperty(globalKey(BINLOG_ACK_COUNT_KEY)), true);
		onEventRetryCount = (Integer) genConfig(onEventRetryCount, configCache.getIntProperty(globalKey(ONEVENT_RETRY_COUNT_KEY)), true);

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

	public Integer getOnEventRetryCount() {
		return onEventRetryCount;
	}

	public void setClient(PumaClient client) {
		this.client = client;
	}

	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}
}
