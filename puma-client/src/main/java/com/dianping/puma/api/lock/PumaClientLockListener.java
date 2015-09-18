package com.dianping.puma.api.lock;

import com.dianping.puma.core.lock.DistributedLockLostListener;

public interface PumaClientLockListener extends DistributedLockLostListener {

	@Override
	public void onLost();
}
