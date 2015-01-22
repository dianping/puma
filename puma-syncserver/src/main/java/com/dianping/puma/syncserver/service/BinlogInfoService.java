package com.dianping.puma.syncserver.service;

import com.dianping.puma.core.sync.model.BinlogInfo;
<<<<<<< HEAD
import com.dianping.puma.core.sync.model.task.SyncTask;
=======
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc

import java.util.List;

public interface BinlogInfoService {
	public void setBaseDir(String baseDir);

<<<<<<< HEAD
	public BinlogInfo getBinlogInfo(long taskId);

	public void saveBinlogInfo(long taskId, BinlogInfo binlogInfo);

	public void removeBinlogInfo(long taskId);

	public List<Long> findSyncTaskIds();
=======
	public BinlogInfo getBinlogInfo(String clientName);

	public void saveBinlogInfo(String clientName, BinlogInfo binlogInfo);

	public void removeBinlogInfo(String clientName);

	public List<String> findSyncTaskClientNames();
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
}
