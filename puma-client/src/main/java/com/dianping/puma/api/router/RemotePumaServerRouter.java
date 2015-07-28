package com.dianping.puma.api.router;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemotePumaServerRouter implements PumaServerRouter {

	private final static String REMOTE_PUMA_SERVER_ROUTER_HOST_KEY = "puma.server.router.host";

	protected final HttpClient httpClient = HttpClients.createDefault();

	protected final ConfigCache configCache = ConfigCache.getInstance();

	protected final Gson gson = new Gson();

	protected String remotePumaServerRouterHost;

	private String database;

	private List<String> tables;

	@Override
	public void init(String database, List<String> tables) {
		this.database = database;
		this.tables = tables;

		remotePumaServerRouterHost = configCache.getProperty(REMOTE_PUMA_SERVER_ROUTER_HOST_KEY);

		configCache.addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (key.equalsIgnoreCase(REMOTE_PUMA_SERVER_ROUTER_HOST_KEY)) {
					remotePumaServerRouterHost = value;
				}
			}
		});
	}

	@Override
	public Map<String, Double> route() {
		String url = genUrl(database, tables);
		HttpGet httpGet = new HttpGet(url);

		try {
			HttpResponse response = httpClient.execute(httpGet);
			String resultString = EntityUtils.toString(response.getEntity());
			@SuppressWarnings("unchecked")
			Map<String, Double> pumaServers = (Map<String, Double>) gson.fromJson(resultString, Map.class);
			return pumaServers;
		} catch (Exception e) {
			// @TODO.
			throw new RuntimeException("route puma server failure.", e.getCause());
		}
	}

	protected String genUrl(String database, List<String> tables) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("database", database));
		for (String table: tables) {
			nameValuePairs.add(new BasicNameValuePair("tables", table));
		}
		String queryString = URLEncodedUtils.format(nameValuePairs, "utf-8");

		return (new StringBuilder())
				.append(remotePumaServerRouterHost)
				.append('?')
				.append(queryString)
				.toString();
	}

	@Override public void addListener(PumaServerRouterListener listener) {

	}
}
