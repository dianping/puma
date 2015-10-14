package com.dianping.puma.storage.manage;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.core.model.BinlogInfo;

import java.io.IOException;

public interface ReadManager extends LifeCycle {

	void openByBinlog(BinlogInfo binlogInfo) throws IOException;

	void openByTime(long timestamp) throws IOException;

	void openFirst() throws IOException;

	void openLast() throws IOException;

	byte[] next() throws IOException;
}
