package com.dianping.puma.core.config;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LionConfigManager implements ConfigManager {

	private final String lionCreateUrl
			= "http://lionapi.dp:8080/config2/create?id=2&project=%s&key=%s&desc=%s";
	private final String lionSetUrl
			= "http://lionapi.dp:8080/config2/set?id=2&env=%s&key=%s&value=%s";

	protected final HttpClient httpClient = HttpClients.createDefault();

	protected final ConfigCache cc = ConfigCache.getInstance();

	protected volatile List<String> keys = new ArrayList<String>();

	protected ConcurrentMap<String, String> cache = new ConcurrentHashMap<String, String>();

	protected ConfigChange configChange;

	protected ConcurrentMap<String, ConfigChangeListener> listeners = new ConcurrentHashMap<String, ConfigChangeListener>();

	@Override
	public void createConfig(String project, String key, String desc) {
		try {
			HttpGet httpGet = new HttpGet(String.format(lionCreateUrl, encode(project), encode(key), encode(desc)));
			HttpResponse httpResponse = httpClient.execute(httpGet);
			String json = EntityUtils.toString(httpResponse.getEntity());
			LionApiResult lionApiResult = new Gson().fromJson(json, LionApiResult.class);
			if (!lionApiResult.getStatus().equalsIgnoreCase("success")) {
				throw new RuntimeException(lionApiResult.getMessage());
			}
		} catch (Throwable t) {
			throw new RuntimeException("failed to create config.", t);
		}
	}

	@Override
	public void setConfig(String key, String value) {
		try {
			HttpGet httpGet = new HttpGet(String.format(lionSetUrl, EnvZooKeeperConfig.getEnv(), encode(key), encode(value)));
			HttpResponse httpResponse = httpClient.execute(httpGet);
			String json = EntityUtils.toString(httpResponse.getEntity());
			LionApiResult lionApiResult = new Gson().fromJson(json, LionApiResult.class);
			if (!lionApiResult.getStatus().equalsIgnoreCase("success")) {
				throw new RuntimeException(lionApiResult.getMessage());
			}
		} catch (Throwable t) {
			throw new RuntimeException("failed to set config.", t);
		}
	}

	@Override
	public String getConfig(String key) {
		String value = cc.getProperty(key);
		if (value != null) {
			cache.put(key, value);
		}
		return value;
	}

	@Override
	public void addConfigChangeListener(final String key, final ConfigChangeListener listener) {
		keys.add(key);
		listeners.put(key, listener);

		if (configChange == null) {
			configChange = new ConfigChange() {
				@Override
				public void onChange(String k, String v) {
					if (keys.contains(k)) {
						ConfigChangeListener configChangeListener = listeners.get(k);
						String oldValue = cache.get(k);
						cache.put(k, v);
						configChangeListener.onConfigChange(oldValue, v);
					}
				}
			};
			cc.addChange(configChange);
		}
	}

	@Override
	public void removeConfigChangeListener(String key, ConfigChangeListener listener) {
		keys.remove(key);
		listeners.remove(key, listener);
	}

	protected String encode(String url) {
		try {
			return URLEncoder.encode(url, "utf-8");
		} catch (Throwable t) {
			throw new RuntimeException("failed to encode url.");
		}
	}

	protected String decode(String url) {
		try {
			return URLDecoder.decode(url, "utf-8");
		} catch (Throwable t) {
			throw new RuntimeException("failed to decode url.");
		}
	}

	private class LionApiResult {
		String status;
		String message;
		String result;

		public String getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}

		public String getResult() {
			return result;
		}
	}
}
