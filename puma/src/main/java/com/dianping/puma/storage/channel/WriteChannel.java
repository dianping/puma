package com.dianping.puma.storage.channel;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;

import java.io.IOException;

public interface WriteChannel extends LifeCycle {

	void append(BinlogInfo binlogInfo, ChangedEvent binlogEvent) throws IOException;

	void flush() throws IOException;
}
