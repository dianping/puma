package com.dianping.puma.admin.ds.extension;

import com.dianping.puma.admin.ds.Cluster;
import com.dianping.puma.admin.ds.Single;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBA {

	private static String url = "http://192.168.222.156:5000/CLUSTER/cluster_status";

	public static List<Cluster> query() {
		try {
			HttpResponse<JsonNode> response = Unirest.get(url).queryString("data", "{}").asJson();
			JSONObject jsonObject = response.getBody().getObject().getJSONObject("data").getJSONObject("mha");
			Map<String, List<DBACluster>> dbaClusters
					= new Gson().fromJson(jsonObject.toString(), new TypeToken<Map<String, List<DBACluster>>>(){}.getType());

			return map(dbaClusters);

		} catch (Throwable t) {
			throw new RuntimeException("failed to query dba", t);
		}
	}

	protected static List<Cluster> map(Map<String, List<DBACluster>> dbaClusters) {
		List<Cluster> clusters = new ArrayList<Cluster>();

		for (Map.Entry<String, List<DBACluster>> entry: dbaClusters.entrySet()) {
			Cluster cluster = new Cluster();
			String clusterName = entry.getKey();
			cluster.setName(clusterName);

			List<DBACluster> dbaClusterList = entry.getValue();
			for (DBACluster dbaCluster: dbaClusterList) {
				Single single = new Single();

				String role = dbaCluster.getRole();
				if (role != null) {
					if (role.equalsIgnoreCase("master")) {
						single.setMaster(true);
					}
				}

				single.setRip(dbaCluster.getRIP());
				single.setVip(dbaCluster.getVIP());
				single.setVersion(dbaCluster.getVersion());

				cluster.addSingle(single);
			}

			clusters.add(cluster);
		}

		return clusters;
	}

	private class DBACluster {
		private String RIP;
		private String VIP;
		private String version;
		private String Role;
		private int port;

		public String getRIP() {
			return RIP;
		}

		public String getVIP() {
			return VIP;
		}

		public String getVersion() {
			return version;
		}

		public String getRole() {
			return Role;
		}

		public int getPort() {
			return port;
		}
	}
}
