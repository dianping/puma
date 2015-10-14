package com.dianping.puma.storage.manage;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;

import java.io.IOException;

public interface ReadManager extends LifeCycle {

	void openOldest() throws IOException;

	void openLatest() throws IOException;

	void open(BinlogInfo binlogInfo) throws IOException;

	ChangedEvent next() throws IOException;
}
