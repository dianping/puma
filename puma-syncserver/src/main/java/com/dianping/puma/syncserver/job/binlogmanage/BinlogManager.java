package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.syncserver.job.LifeCycle;

public interface BinlogManager extends LifeCycle {

	public void before(long seq, BinlogInfo binlogInfo);

	public void after(long seq, BinlogInfo binlogInfo);

	public BinlogInfo getBinlogInfo();

	public long getSeq();
}
