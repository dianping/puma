package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.syncserver.job.LifeCycle;

public interface BinlogManager extends LifeCycle {

	/** Record before the binlog event is synchronized. */
	public void before(BinlogInfo binlogInfo);

	/** Record after the binlog event is synchronized. */
	public void after(BinlogInfo binlogInfo);

	/** Get the recovery binlog info point. */
	public BinlogInfo getRecovery();

	public void removeRecovery();
}
