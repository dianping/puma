package com.dianping.puma.core.zk;

import com.dianping.lion.EnvZooKeeperConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LionZkManager implements ZkManager {

	private final Logger logger = LoggerFactory.getLogger(LionZkManager.class);

	private final int defaultSessionTimeoutMs = 30 * 1000; // 30s.

	private final int defaultConnectionTimeoutMs = 15 * 1000; // 15s.

	private final RetryPolicy defaultRetryPolicy = new ExponentialBackoffRetry(1000, 3);

	private volatile CuratorFramework zkClient;

	@Override
	public CuratorFramework getZkClient() {
		return getZkClient(defaultSessionTimeoutMs, defaultConnectionTimeoutMs, defaultRetryPolicy);
	}

	protected CuratorFramework getZkClient(int sessionTimeoutMs, int connectionTimeoutMs,
			RetryPolicy retryPolicy) {
		if (zkClient != null) {
			return zkClient;
		}

		String zkAddress = EnvZooKeeperConfig.getZKAddress();
		zkClient = CuratorFrameworkFactory.newClient(zkAddress, sessionTimeoutMs, connectionTimeoutMs, retryPolicy);
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
