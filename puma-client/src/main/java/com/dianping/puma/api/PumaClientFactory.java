package com.dianping.puma.api;

import com.dianping.puma.api.impl.ClusterPumaClient;
import com.dianping.puma.api.impl.SimplePumaClient;

public class PumaClientFactory {

	public static PumaClient createSimplePumaClient(String clientName, String pumaServerHost) {
		return new SimplePumaClient(clientName, pumaServerHost);
	}

	public static PumaClient createClusterPumaClient(String clientName) {
		return new ClusterPumaClient(clientName);
	}
}
