package com.dianping.puma.api.router;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZookeeperPumaServerRouter implements PumaServerRouter {

	private String zkPath;

	protected ConfigCache configCache = ConfigCache.getInstance();

	@Override
	public void init(String database, List<String> tables) {
		zkPath = parseZkPath(database, tables);
	}

	@Override
	public Map<String, Double> route() {
		String str = configCache.getProperty(zkPath);
		return parsePumaServers(str);
	}

	@Override
	public void addListener(final PumaServerRouterListener listener) {
		configCache.addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (key.equalsIgnoreCase(zkPath)) {
					listener.onChange(parsePumaServers(value));
				}
			}
		});
	}

	protected String parseZkPath(String database, List<String> tables) {
		return (new StringBuilder()).append("puma.client.route.").append(database).toString();
	}

	protected Map<String, Double> parsePumaServers(String str) {
		Map<String, Double> result = new HashMap<String, Double>();

		if (str == null) {
			return result;
		}

		String[] pumaServers = StringUtils.split(str);
		if (pumaServers != null) {
			for (String pumaServer: pumaServers) {
				String[] pair = StringUtils.split(pumaServer, "#");
				if (pair != null && pair.length == 2) {
					result.put(pair[0], Double.valueOf(pair[1]));
				}
			}
		}

		return result;
	}
}
