package com.dianping.puma.portal.model;

import java.util.*;

public class PumaDto {

	private String database;

	private List<String> tables;

	private List<String> serverNames;

	private Map<String, String> hosts;

	private Map<String, Date> beginTimes;

	private Map<String, Long> beginTimestamps;

	private Map<String, Boolean> actives;

	private Map<String, Double> weights;

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public List<String> getTables() {
		return tables;
	}

	public void setTables(List<String> tables) {
		this.tables = tables;
	}

	public List<String> getServerNames() {
		return serverNames;
	}

	public void setServerNames(List<String> serverNames) {
		this.serverNames = serverNames;
	}

	public void addServerName(String server) {
		if (serverNames == null) {
			serverNames = new ArrayList<String>();
		}

		serverNames.add(server);
	}

	public Map<String, String> getHosts() {
		return hosts;
	}

	public void setHosts(Map<String, String> hosts) {
		this.hosts = hosts;
	}

	public void addHost(String serverName, String host) {
		if (hosts == null) {
			hosts = new HashMap<String, String>();
		}

		hosts.put(serverName, host);
	}

	public Map<String, Boolean> getActives() {
		return actives;
	}

	public void setActives(Map<String, Boolean> actives) {
		this.actives = actives;
	}

	public void addActive(String serverName, Boolean active) {
		if (actives == null) {
			actives = new HashMap<String, Boolean>();
		}

		actives.put(serverName, active);
	}

	public Map<String, Long> getBeginTimestamps() {
		return beginTimestamps;
	}

	public void setBeginTimestamps(Map<String, Long> beginTimestamps) {
		this.beginTimestamps = beginTimestamps;
	}

	public Map<String, Date> getBeginTimes() {
		return beginTimes;
	}

	public void setBeginTimes(Map<String, Date> beginTimes) {
		this.beginTimes = beginTimes;
	}

	public void addBeginTime(String serverName, Date beginTime) {
		if (beginTimes == null) {
			beginTimes = new HashMap<String, Date>();
		}

		beginTimes.put(serverName, beginTime);
	}

	public Map<String, Double> getWeights() {
		return weights;
	}

	public void setWeights(Map<String, Double> weights) {
		this.weights = weights;
	}

	public void addWeight(String serverName, Double weight) {
		if (weights == null) {
			weights = new HashMap<String, Double>();
		}

		weights.put(serverName, weight);
	}
}
