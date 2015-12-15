package com.dianping.puma.common;

import com.dianping.puma.config.ConfigChangeListener;
import com.dianping.puma.config.ConfigManager;
import com.dianping.puma.utils.IPUtils;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RootLoggerLevelConfig implements InitializingBean {

	private static final String ROOT_LOGGER_LEVEL_KEY = "puma.server.rootlogger.level";

	private static final Map<String, Level> LEVEL_MAPPING;

	@Autowired
	ConfigManager configManager;

	private Gson gson = new GsonBuilder().create();

	static {
		LEVEL_MAPPING = new HashMap<String, Level>();
		LEVEL_MAPPING.put("off", Level.OFF);
		LEVEL_MAPPING.put("trace", Level.TRACE);
		LEVEL_MAPPING.put("debug", Level.DEBUG);
		LEVEL_MAPPING.put("info", Level.INFO);
		LEVEL_MAPPING.put("warn", Level.WARN);
		LEVEL_MAPPING.put("error", Level.ERROR);
		LEVEL_MAPPING.put("fatal", Level.FATAL);
		LEVEL_MAPPING.put("all", Level.ALL);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		String formatRootLoggerLevel = configManager.getConfig(ROOT_LOGGER_LEVEL_KEY);
		check(formatRootLoggerLevel);

		configManager.addConfigChangeListener(ROOT_LOGGER_LEVEL_KEY, new ConfigChangeListener() {
			@Override
			public void onConfigChange(String oldValue, String newValue) {
				check(newValue);
			}
		});
	}

	protected void check(String formatRootLoggerLevel) {
		Map<String, String> rawRootLoggerLevel = parseRootLoggerLevel(formatRootLoggerLevel);
		Map<String, String> rootLoggerLevel = normalizeRootLoggerLevel(rawRootLoggerLevel);
		changeLoggerLevel(rootLoggerLevel);
	}

	protected Map<String, String> parseRootLoggerLevel(String formatRootLoggerLevel) {
		try {
			Map<String, String> json = gson.fromJson(formatRootLoggerLevel, new TypeToken<Map<String, String>>(){}.getType());
			if (json == null) {
				throw new NullPointerException("no format root logger level found.");
			}
			return json;
		} catch (Throwable t) {
			return Maps.newHashMap();
		}
	}

	protected Map<String, String> normalizeRootLoggerLevel(Map<String, String> rawRootLoggerMap) {
		Map<String, String> rootLoggerMap = new HashMap<String, String>();
		for (Map.Entry<String, String> entry: rawRootLoggerMap.entrySet()) {
			String ip = StringUtils.normalizeSpace(entry.getKey());
			String level = StringUtils.normalizeSpace(entry.getValue());
			rootLoggerMap.put(ip, level);
		}
		return rootLoggerMap;
	}

	protected void changeLoggerLevel(Map<String, String> rootLoggerMap) {
		for (String ip: IPUtils.getNoLoopbackIP4Addresses()) {
			String level = rootLoggerMap.get(ip);
			if (level != null) {
				Level _level = LEVEL_MAPPING.get(level.toLowerCase());
				if (_level != null) {
					LogManager.getRootLogger().setLevel(_level);
				}
			}
		}
	}
}