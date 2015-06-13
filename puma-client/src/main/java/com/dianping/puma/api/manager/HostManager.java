package com.dianping.puma.api.manager;

import com.dianping.puma.api.exception.PumaException;
import com.dianping.puma.core.LifeCycle;

public interface HostManager extends LifeCycle<PumaException> {

	String next();

	String current();

	void feedback(Feedback state);
}
