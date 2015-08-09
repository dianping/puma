package com.dianping.puma.admin.ds.extension;

import com.dianping.lion.client.ConfigCache;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

import java.util.*;

public class Zebra {

	private static final String url = "http://lionapi.dp:8080/config2/get";

	protected static ConfigCache cc = ConfigCache.getInstance();

	public Map<String, Pair<List<String>, List<String>>> query() {
		try {
			HttpResponse<JsonNode> jsonNode = Unirest.get(url)
					.queryString("prefix", "groupds")
					.queryString("env", "dev")
					.queryString("id", 2)
					.asJson();

			JSONObject jsonObject = jsonNode.getBody().getObject().getJSONObject("result");
			Map<String, String> zebraGroupDsMap = new Gson().fromJson(
					jsonObject.toString(), new TypeToken<Map<String, String>>(){}.getType());

			Map<String, Pair<List<String>, List<String>>> response = new HashMap<String, Pair<List<String>, List<String>>>();
			for (Map.Entry<String, String> entry: zebraGroupDsMap.entrySet()) {
				String jdbcRef = parseGroupDsName(entry.getKey());
				Pair<List<String>, List<String>> groupDsNames = parseGroupDsNames(entry.getValue());
				response.put(jdbcRef, groupDsNames);
			}

			return response;

		} catch (Throwable t) {
			throw new RuntimeException("failed to query zebra ds.", t);
		}
	}

	protected String parseGroupDsName(String groupDsPath) {
		return StringUtils.substringBetween(groupDsPath, "groupds.", ".mapping");
	}

	protected Pair<List<String>, List<String>> parseGroupDsNames(String groupDsNames) {
		List<String> readDsNames = new ArrayList<String>();
		List<String> writeDsNames = new ArrayList<String>();

		String[] all = StringUtils.substringsBetween(groupDsNames, "(", ")");
		if (all == null) {
			return null;
		}

		String[] left = null;
		if (all.length >= 1) {
			left = parseDsNames(all[0]);
		}

		String[] right = null;
		if (all.length >= 2) {
			right = parseDsNames(all[1]);
		}

		if (isReadDsNames(all[0])) {
			readDsNames.addAll(Arrays.asList(left));
			writeDsNames.addAll(Arrays.asList(right));
		} else {
			readDsNames.addAll(Arrays.asList(right));
			writeDsNames.addAll(Arrays.asList(left));
		}

		return Pair.of(readDsNames, writeDsNames);
	}

	protected boolean isReadDsNames(String rawDsNames) {
		return rawDsNames.contains("-read");
	}

	protected boolean isWriteDsNames(String rawDsNames) {
		return rawDsNames.contains("-write");
	}

	protected String[] parseDsNames(String rawDsNames) {
		String[] dsNames = StringUtils.split(rawDsNames, ",");
		if (dsNames == null) {
			return null;
		}
		for (int i = 0; i != dsNames.length; ++i) {
			dsNames[i] = StringUtils.substringBefore(dsNames[i], ":");
		}

		return dsNames;
	}

	protected String queryIp(String dsName) {
		String jdbcUrlPath = buildJdbcUrlPath(dsName);
		String jdbcUrl = cc.getProperty(jdbcUrlPath);
		return parseIp(jdbcUrl);
	}

	protected String buildJdbcUrlPath(String dsName) {
		return "ds." + dsName + ".jdbc.url";
	}

	protected String parseIp(String jdbcUrl) {
		return StringUtils.substringBetween(jdbcUrl, "//", "/");
	}
}
