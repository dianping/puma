package com.dianping.puma.core.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;

public interface ZkManager {

	public CuratorFramework getZkClient();

	public CuratorFramework getZkClient(int sessionTimeoutMs, int connectionTimeoutMs, RetryPolicy retryPolicy);
}
