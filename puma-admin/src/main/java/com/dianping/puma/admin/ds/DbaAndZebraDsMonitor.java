package com.dianping.puma.admin.ds;

import com.dianping.puma.core.config.ConfigManager;
import com.dianping.zebra.group.config.DataSourceConfigManager;
import com.dianping.zebra.group.config.DataSourceConfigManagerFactory;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;
import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class DbaAndZebraDsMonitor implements DsMonitor {

	private String queryClustersUrl;

	private String queryDatabasesUrl;

	protected ConfigManager configManager;

	protected ConcurrentMap<String, Cluster> clusters;

	@Override
	public Cluster getCluster(String clusterName) {
		return null;
	}

	@Override
	public void addListener(String clusterName, DsMonitorListener listener) {

	}

	@Override
	public void removeListener(String clusterName) {

	}

	protected Map<String, List<Single>> queryClusters() {
		return null;
	}

	protected List<String> queryDatabases() {
		try {
			HttpResponse<JsonNode> response = Unirest.get(queryDatabasesUrl).asJson();
			Map<String, String> raw = new Gson().fromJson(
					response.getBody().getObject().getString("result"),
					new TypeToken<Map<String, String>>(){}.getType());
			return new ArrayList<String>(raw.keySet());
		} catch (Throwable t) {
			throw new RuntimeException("failed to query databases.", t);
		}
	}

	protected Map<String, List<String>> queryDatabaseHosts(List<String> databases) {
		Map<String, List<String>> databaseHosts = new HashMap<String, List<String>>();

		for (String database: databases) {
			DataSourceConfigManager manager = DataSourceConfigManagerFactory.getConfigManager("remote", database);
			manager.init();

			GroupDataSourceConfig config = manager.getGroupDataSourceConfig();
			Map<String, DataSourceConfig> dataSourceConfigs = config.getDataSourceConfigs();

			List<String> hosts = new ArrayList<String>();
			for (DataSourceConfig dataSourceConfig: dataSourceConfigs.values()) {
				String jdbcUrl = dataSourceConfig.getJdbcUrl();
				String host = parseHostFromJdbcUrl(jdbcUrl);
				hosts.add(host);
			}

			databaseHosts.put(database, hosts);
		}

		return databaseHosts;
	}

	protected String parseHostFromJdbcUrl(String jdbcUrl) {
		return StringUtils.substringBetween(jdbcUrl, "//", "/");
	}

	protected Map<String, List<String>> findMasterDbRelation(Map<String, List<DataSourceConfig>> dbDsRelation) {
		return null;
	}

	protected Map<String, List<String>> findSlaveDbRelation(Map<String, List<DataSourceConfig>> dbDsRelation) {
		return null;
	}

	protected Map<String, Cluster> combine(Map<String, Cluster> rawClusters,
			Map<String, List<String>> masterDbRelation, Map<String, List<String>> slaveDbRelation) {
		return null;
	}
}
