package com.dianping.puma.syncserver.job;

import org.apache.commons.lang3.exception.ContextedRuntimeException;

public interface LifeCycle<T extends ContextedRuntimeException> {

	// Start.
	public void start();

	// Stop.
	public void stop();

	// Clean all the persistent storage.
	public void die();

	public T exception();
}
