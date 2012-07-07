package com.dianping.puma.storage;

import java.io.IOException;

import com.dianping.puma.core.event.ChangedEvent;

public interface EventStorage {
	public EventChannel getChannel(long seq) throws IOException;

	public void store(ChangedEvent event) throws IOException;

	public void close();
}
