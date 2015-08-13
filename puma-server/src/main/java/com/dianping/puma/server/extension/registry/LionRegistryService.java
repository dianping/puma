package com.dianping.puma.server.extension.registry;

import com.dianping.puma.core.config.ConfigManager;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LionRegistryService implements RegistryService {

	private final Logger logger = LoggerFactory.getLogger(LionRegistryService.class);

	@Autowired
	ConfigManager configManager;

	@Override
	public void register(String host, String database, List<String> tables) {
		String hostListString = configManager.getConfig(buildKey(database));

		if (hostListString == null) {
			// maybe not created or created but not set.
			try {
				configManager.createConfig("puma", buildKey(database), database);
			} catch (Throwable t) {
				logger.warn("failed to create config");
			} finally {
				hostListString = "";
			}
		}

		List<String> hostList = parseHostList(hostListString);

		if (!hostList.contains(host)) {
			hostList.add(host);
			hostListString = buildHostListString(hostList);
			configManager.setConfig(buildKey(database), hostListString);
		}
	}

	@Override
	public void unregister(String host, String database, List<String> tables) {
		String hostListString = configManager.getConfig(buildKey(database));

		if (hostListString != null) {
			List<String> hostList = parseHostList(hostListString);
			hostList.remove(host);
			hostListString = buildHostListString(hostList);
			configManager.setConfig(buildKey(database), hostListString);
		}
	}

	protected List<String> parseHostList(String hostListString) {
		return Lists.newArrayList(StringUtils.split(hostListString, "#"));
	}

	protected String buildHostListString(List<String> hostList) {
		return StringUtils.join(hostList, "#");
	}

	protected String buildKey(String database) {
		return "puma.client.route." + database;
	}
}
