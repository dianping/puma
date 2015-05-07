package com.dianping.puma.syncserver.job.load.model;

import com.dianping.puma.core.event.ChangedEvent;

public class RowPool {

	/**
	 * Inject the given row into the row pool if all the conditions are met.
	 *
	 * @param row
	 * @return
	 */
	public boolean inject(ChangedEvent row) {
		return false;
	}

	/**
	 * Extract the given row out of the row pool.
	 *
	 * @param row
	 */
	public void extract(ChangedEvent row) {

	}

	/**
	 * Check whether the given row is the first injected row in the pool.
	 *
	 * @param row
	 * @return
	 */
	public boolean isFirst(ChangedEvent row) {
		return false;
	}

	public void clear() {

	}
}
