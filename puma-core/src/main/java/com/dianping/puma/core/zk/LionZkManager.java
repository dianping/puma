package com.dianping.puma.core.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Service;

@Service
public class LionZkManager implements ZkManager {

	@Override
	public CuratorFramework getZkClient() {
		return null;
	}

	@Override
	public CuratorFramework getZkClient(int sessionTimeoutMs, int connectionTimeoutMs,
			RetryPolicy retryPolicy) {
		return null;
	}
}
