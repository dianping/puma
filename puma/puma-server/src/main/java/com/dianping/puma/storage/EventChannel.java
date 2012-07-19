package com.dianping.puma.storage;

import java.io.IOException;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.exception.StorageException;

public interface EventChannel {
	/**
	 * close the channel.
	 */
	public void close();

	/**
	 * return next event from the channel. It will be blocked until an event is
	 * available.
	 * 
	 * @return event which is not null
	 * @throws IOException
	 */
	public ChangedEvent next() throws StorageException;
}
