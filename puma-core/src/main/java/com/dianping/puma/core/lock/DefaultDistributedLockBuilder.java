package com.dianping.puma.core.lock;

import com.dianping.puma.core.zk.ZkManager;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultDistributedLockBuilder implements DistributedLockBuilder {

	@Autowired
	ZkManager zkManager;

	@Override
	public DistributedLock buildZkDistributedLock(String lockName) {
		CuratorFramework zkClient = zkManager.getZkClient();
		return new ZkDistributedLock(lockName, zkClient);
	}

	@Override
	public DistributedLock buildLionDistributedLock(String lockName) {
		return null;
	}
}
