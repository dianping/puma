package com.dianping.puma.api.manager;

import com.dianping.puma.api.exception.PumaException;
import com.dianping.puma.core.LifeCycle;

public interface HeartbeatManager extends LifeCycle<PumaException> {

	void open();

	void close();

	void heartbeat();
}
