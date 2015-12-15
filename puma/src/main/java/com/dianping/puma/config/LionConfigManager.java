package com.dianping.puma.config;

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

import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LionConfigManager implements ConfigManager {

	private final String lionCreateUrl = "http://lionapi.dp:8080/config2/create?id=2&project=%s&key=%s&desc=%s";

	private final String lionSetUrl = "http://lionapi.dp:8080/config2/set?id=2&env=%s&key=%s&value=%s";

	private final String getProjectConfigUrl = "http://lionapi.dp:8080/config2/get?env=%s&project=%s&id=2";

	protected HttpClient httpClient = HttpClients.createDefault();

	protected ConfigCache cc = ConfigCache.getInstance();

	protected ConfigChange configChange;

	protected ConcurrentMap<String, Boolean> keys = new ConcurrentHashMap<String, Boolean>();

	protected ConcurrentMap<String, String> caches = new ConcurrentHashMap<String, String>();

	protected ConcurrentMap<String, WeakReference<ConfigChangeListener>> listeners = new ConcurrentHashMap<String, WeakReference<ConfigChangeListener>>();

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
	public Map<String, String> getConfigByProject(String env, String project) {
		try {
			HttpGet httpGet = new HttpGet(String.format(getProjectConfigUrl, encode(env), encode(project)));
			HttpResponse httpResponse = httpClient.execute(httpGet);

			String json = EntityUtils.toString(httpResponse.getEntity());

			LionGetByProject result = new Gson().fromJson(json, LionGetByProject.class);

			if (!result.getStatus().equalsIgnoreCase("success")) {
				throw new RuntimeException(result.getMessage());
			} else {
				return result.getResult();
			}
		} catch (Throwable t) {
			throw new RuntimeException("failed to get config by project.", t);
		}
	}

	@Override
	public void setConfig(String key, String value) {
		try {
			HttpGet httpGet = new HttpGet(String.format(lionSetUrl, EnvZooKeeperConfig.getEnv(), encode(key),
			      encode(value)));
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
			caches.put(key, value);
		}
		return value;
	}

	@Override
	public void addConfigChangeListener(final String key, final ConfigChangeListener listener) {
		keys.put(key, true);
		listeners.put(key, new WeakReference<ConfigChangeListener>(listener));

		if (configChange == null) {
			configChange = new ConfigChange() {
				@Override
				public void onChange(String k, String v) {
					if (keys.containsKey(k)) {
						ConfigChangeListener configChangeListener = listeners.get(k).get();

						if (configChangeListener == null) {
							// Object held by weak reference is garbage collected.
							keys.remove(k);
							caches.remove(k);
							listeners.remove(k);
						} else {
							String oldValue = caches.get(k);
							caches.put(k, v);
							configChangeListener.onConfigChange(oldValue, v);
						}
					}
				}
			};
			cc.addChange(configChange);
		}
	}

	@Override
	public void removeConfigChangeListener(String key, ConfigChangeListener listener) {
		keys.remove(key);
		caches.remove(key);
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

	public class LionGetByProject {
		String status;

		String message;

		Map<String, String> result;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public Map<String, String> getResult() {
			return result;
		}

		public void setResult(Map<String, String> result) {
			this.result = result;
		}
	}

	public class LionApiResult {
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