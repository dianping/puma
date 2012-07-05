package com.dianping.puma.storage;

import java.io.IOException;

public interface EventStorage {
	public EventChannel getChannel(long seq) throws IOException;
}
