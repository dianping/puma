package com.dianping.puma.server.extension.registry;

import com.dianping.puma.core.config.ConfigManager;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class LionRegistryService implements RegistryService {

	@Autowired
	ConfigManager configManager;

	@Override
	public void register(String host, String database, List<String> tables) {
		String hostListString = configManager.getConfig(database);

		if (hostListString == null) {
			// maybe not created or created but not set.
			try {
				configManager.createConfig("puma", database, database + " puma server list");
			} catch (Throwable t) {
				// Nothing to do here.
			} finally {
				hostListString = "";
			}
		}

		List<String> hostList = parseHostList(hostListString);

		if (!hostList.contains(host)) {
			hostList.add(host);
			hostListString = buildHostListString(hostList);
			configManager.setConfig(database, hostListString);
		}
	}

	@Override
	public void unregister(String host, String database, List<String> tables) {
		String hostListString = configManager.getConfig(database);

		if (hostListString != null) {
			List<String> hostList = parseHostList(hostListString);
			hostList.remove(host);
			hostListString = buildHostListString(hostList);
			configManager.setConfig(database, hostListString);
		}
	}

	protected List<String> parseHostList(String hostListString) {
		return Lists.newArrayList(StringUtils.split(hostListString));
	}

	protected String buildHostListString(List<String> hostList) {
		return StringUtils.join(hostList, "\t");
	}
}
