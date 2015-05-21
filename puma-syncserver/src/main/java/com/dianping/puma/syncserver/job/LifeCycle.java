package com.dianping.puma.syncserver.job;

import org.apache.commons.lang3.exception.ContextedRuntimeException;

public interface LifeCycle<T extends ContextedRuntimeException> {

	public void start();

	public void stop();

	public void destroy();

	public T exception();
}
