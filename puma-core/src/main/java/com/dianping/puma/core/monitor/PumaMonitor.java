package com.dianping.puma.core.monitor;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.core.exception.MonitorException;

public interface PumaMonitor extends LifeCycle<MonitorException> {

	void start();

	void stop();

	void pause();

	void record(String name, String status);
}
