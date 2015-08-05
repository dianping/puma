package com.dianping.puma.api;

import com.dianping.puma.api.impl.*;

import java.util.List;

public class PumaClientFactory {

	public static PumaClient createSimplePumaClient(String name, String serverHost) {
		return new SimplePumaClient(name, serverHost);
	}

	public static PumaClient createClusterPumaClient(String name) {
		return createZookeeperClusterPumaClient(name);
	}

	public static PumaClient createClusterPumaClient(String name, List<String> serverHosts) {
		ConstantPumaServerMonitor monitor = new ConstantPumaServerMonitor(serverHosts);
		MonitorBasedPumaServerRouter router = new MonitorBasedPumaServerRouter(monitor);
		return new ClusterPumaClient(name, router);
	}

	public static PumaClient createZookeeperClusterPumaClient(String name) {
		ZookeeperPumaServerMonitor monitor = new ZookeeperPumaServerMonitor();
		MonitorBasedPumaServerRouter router = new MonitorBasedPumaServerRouter(monitor);
		return new ClusterPumaClient(name, router);
	}
}
