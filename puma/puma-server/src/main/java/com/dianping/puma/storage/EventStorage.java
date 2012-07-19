package com.dianping.puma.storage;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.exception.StorageException;

public interface EventStorage {
	public EventChannel getChannel(long seq) throws StorageException;

	public void store(ChangedEvent event) throws StorageException;

	public void close();
}
