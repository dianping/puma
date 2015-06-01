package com.dianping.puma.syncserver.job;

public interface LifeCycle {

	void init();

	void destroy();

	void start();

	void stop();
}
