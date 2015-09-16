package com.dianping.puma.core.zk;

public class ZkManagerLoader {

	private static ZkManager lionZkManager = new LionZkManager();

	public static ZkManager getLionZkManager() {
		if (lionZkManager == null) {
			lionZkManager = new LionZkManager();
		}
		return lionZkManager;
	}
}
