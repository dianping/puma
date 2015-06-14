package com.dianping.puma.api.manager;

import com.dianping.puma.api.exception.PumaException;
import com.dianping.puma.core.LifeCycle;

public interface LockManager extends LifeCycle<PumaException> {

	boolean tryLock();

	void unlock();
}
