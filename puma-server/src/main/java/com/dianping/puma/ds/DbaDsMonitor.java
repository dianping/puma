package com.dianping.puma.ds;

import com.dianping.puma.core.config.ConfigChangeListener;
import com.dianping.puma.core.config.ConfigManager;
import com.dianping.puma.core.config.LionConfigManager;
import com.dianping.zebra.biz.service.LionService;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DbaDsMonitor implements DsMonitor {

	private final String DBA_QUERY_URL = "puma.server.dbaquery.url";

	private String dbaQueryUrl;

	private String queryDatabasesUrl;

	protected ConfigManager configManager;

	protected LionService lionService;

	protected HttpClient httpClient = HttpClients.createDefault();

	protected Map<String, Cluster> clusters;

	public DbaDsMonitor() {
		configManager = new LionConfigManager();
		dbaQueryUrl = configManager.getConfig(DBA_QUERY_URL);
		configManager.addConfigChangeListener(DBA_QUERY_URL, new ConfigChangeListener() {
			@Override
			public void onConfigChange(String oldValue, String newValue) {
				dbaQueryUrl = newValue;
			}
		});
	}

	@Override
	public Cluster getCluster(String clusterName) {
		return clusters.get(clusterName);
	}

	@Override
	public void addListener(String clusterName, DsMonitorListener listener) {

	}

	@Override
	public void removeListener(String clusterName) {

	}

	@Scheduled(fixedDelay = 60 * 1000)
	public void scheduleDbaQuery() {
		clusters = dbaQuery();
	}

	protected Map<String, Cluster> dbaQuery() {
		try {
			HttpGet httpGet = new HttpGet(dbaQueryUrl);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			String json = EntityUtils.toString(httpResponse.getEntity());
			DbaResult dbaResult = new Gson().fromJson(json, DbaResult.class);

			if (!dbaResult.status.equalsIgnoreCase("0")) {
				throw new RuntimeException("failed to query dba, status not equal to 0.");
			}

			return mapDbaResult(dbaResult);
		} catch (Throwable t) {
			throw new RuntimeException("failed to query dba.", t);
		}
	}

	protected Map<String, Cluster> mapDbaResult(DbaResult result) {
		Map<String, Cluster> clusters = new HashMap<String, Cluster>();

		for (Map.Entry<String, List<DbaResult.Data.Mha>> entry: result.data.mha.entrySet()) {
			Cluster cluster = new Cluster();

			String mhaName = entry.getKey();
			cluster.setName(mhaName);

			List<DbaResult.Data.Mha> mhaInstances = entry.getValue();
			for (DbaResult.Data.Mha mhaInstance: mhaInstances) {
				Single single = new Single();
				single.setVersion(mhaInstance.version);
				single.setRip(mhaInstance.RIP);
				single.setVip(mhaInstance.VIP);
				single.setMaster(mhaInstance.Role.equalsIgnoreCase("master"));
				cluster.addSingle(single);
			}

			clusters.put(mhaName, cluster);
		}

		return clusters;
	}

	static class DbaResult {
		public String status;
		public Data data;
		static class Data {
			public Map<String, List<Mha>> mha;
			static class Mha {
				public String Status;
				public String buss;
				private String RIP;
				private String VIP;
				private String version;
				private String MonIP;
				private String Role;
				private String port;
			}
		}
	}
}
