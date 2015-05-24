package com.dianping.puma.syncserver.job;

public interface LifeCycle {

	// Start.
	public void start();

	// Stop.
	public void stop();

	// Clean all the persistent storage.
	public void die();

}
