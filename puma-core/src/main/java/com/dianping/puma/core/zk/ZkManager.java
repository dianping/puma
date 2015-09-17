package com.dianping.puma.core.zk;

import org.apache.curator.framework.CuratorFramework;

public interface ZkManager {

	public CuratorFramework getZkClient();
}
