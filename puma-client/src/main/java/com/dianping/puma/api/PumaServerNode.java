package com.dianping.puma.api;

public class PumaServerNode {

	private String host;

	private double loadBalance;

	public PumaServerNode(String host, double loadBalance) {
		this.host = host;
		this.loadBalance = loadBalance;
	}

	public String getHost() {
		return host;
	}

	public double getLoadBalance() {
		return loadBalance;
	}
}
