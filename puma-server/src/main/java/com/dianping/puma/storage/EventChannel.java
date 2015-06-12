package com.dianping.puma.storage;

import com.dianping.puma.core.event.Event;
import com.dianping.puma.storage.exception.StorageException;

public interface EventChannel {

	public void open();

	public void close();

	public Event next() throws StorageException;
}
