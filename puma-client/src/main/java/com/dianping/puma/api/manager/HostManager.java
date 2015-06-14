package com.dianping.puma.api.manager;

import com.dianping.puma.api.exception.PumaException;
import com.dianping.puma.core.LifeCycle;

public interface HostManager extends LifeCycle<PumaException> {

	/**
	 * Returns the next host to connect according to the hosts list, connection
	 * feedback and the strategies.
	 *
	 * @return next host to connect.
	 */
	String next();

	/**
	 * Returns the current host connecting.
	 *
	 * @return current host connecting.
	 */
	String current();

	/**
	 * Feeds back connection state to the manager for the next host picking.
	 *
	 * @param state connection state.
	 */
	void feedback(Feedback state);
}
