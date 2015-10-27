package com.dianping.puma.storage.channel;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.core.event.ChangedEvent;

import java.io.IOException;

public interface WriteChannel extends LifeCycle {

	void append(ChangedEvent binlogEvent) throws IOException;

	void flush() throws IOException;
}
