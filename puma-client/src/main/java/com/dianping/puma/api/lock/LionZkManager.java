package com.dianping.puma.api.lock;

import com.dianping.lion.EnvZooKeeperConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LionZkManager {

    private final Logger logger = LoggerFactory.getLogger(LionZkManager.class);

    private static final int DEFAULT_SESSION_TIMEOUT_MS = 30 * 1000;

    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 15 * 1000;

    private final RetryPolicy defaultRetryPolicy = new ExponentialBackoffRetry(1000, 3);

    private volatile CuratorFramework zkClient;

    protected LionZkManager() {
    }

    public CuratorFramework getZkClient() {
        return getZkClient(DEFAULT_SESSION_TIMEOUT_MS, DEFAULT_CONNECTION_TIMEOUT_MS, defaultRetryPolicy);
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
