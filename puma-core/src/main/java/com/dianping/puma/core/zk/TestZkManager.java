package com.dianping.puma.core.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestZkManager implements ZkManager {

	private final Logger logger = LoggerFactory.getLogger(TestZkManager.class);

	private final int defaultSessionTimeoutMs = 30 * 1000; // 30s.

	private final int defaultConnectionTimeoutMs = 15 * 1000; // 15s.

	private final RetryPolicy defaultRetryPolicy = new ExponentialBackoffRetry(1000, 3);

	private TestingServer server;

	private CuratorFramework zkClient;

	protected TestZkManager(TestingServer server) {
		this.server = server;
	}

	@Override
	public CuratorFramework getZkClient() {
		return getZkClient(defaultSessionTimeoutMs, defaultConnectionTimeoutMs, defaultRetryPolicy);
	}

	protected CuratorFramework getZkClient(int sessionTimeoutMs, int connectionTimeoutMs,
			RetryPolicy retryPolicy) {
		if (zkClient != null) {
			return zkClient;
		}

		zkClient = CuratorFrameworkFactory.newClient(server.getConnectString(), sessionTimeoutMs, connectionTimeoutMs, retryPolicy);
		zkClient.start();

		try {
			zkClient.getZookeeperClient().blockUntilConnectedOrTimedOut();
		} catch (InterruptedException e) {
			logger.error("zookeeper client start interrupted.", e);
			return null;
		}

		return zkClient;
	}
}
