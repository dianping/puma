package com.dianping.puma.syncserver.service;

import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.SyncTask;

import java.util.List;

public interface BinlogInfoService {
	public void setBaseDir(String baseDir);

	public BinlogInfo getBinlogInfo(long taskId);

	public void saveBinlogInfo(long taskId, BinlogInfo binlogInfo);

	public void removeBinlogInfo(long taskId);

	public List<Long> findSyncTaskIds();
}
