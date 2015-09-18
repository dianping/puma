package com.dianping.puma.core.zk;

import org.apache.curator.test.TestingServer;

public class ZkManagerLoader {

	private static ZkManager lionZkManager;

	private static ZkManager testZkManager;

	public static ZkManager getLionZkManager() {
		if (lionZkManager == null) {
			lionZkManager = new LionZkManager();
		}
		return lionZkManager;
	}

	public static ZkManager getTestZkManager(TestingServer server) {
		if (testZkManager == null) {
			testZkManager = new TestZkManager(server);
		}
		return testZkManager;
	}
}
