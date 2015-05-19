package com.dianping.puma.syncserver.job.binlogmanage;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.syncserver.job.LifeCycle;
import com.dianping.puma.syncserver.job.binlogmanage.exception.BinlogManageException;

public interface BinlogManager extends LifeCycle<BinlogManageException> {

	public void before(BinlogInfo binlogInfo);

	public void after(BinlogInfo binlogInfo);

	public BinlogInfo getEarliest();

	public void delete();
}
