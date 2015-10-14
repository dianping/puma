package com.dianping.puma.storage.manage;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.storage.Sequence;

import java.io.IOException;

public interface WriteManager extends LifeCycle {

	Sequence writeSequence();

	Sequence flushSequence();

	void append(BinlogInfo binlogInfo, ChangedEvent binlogEvent) throws IOException;

	void flush() throws IOException;
}
