package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.syncserver.job.LifeCycle;
import com.dianping.puma.syncserver.job.binlogmanage.exception.BinlogManageException;

public interface BinlogManager extends LifeCycle<BinlogManageException> {

	public void save(BinlogInfo binlogInfo);

	public BinlogInfo getEarliest();
}
