package com.dianping.puma.syncserver.service;

import java.util.List;

import com.dianping.puma.core.sync.model.BinlogInfo;

public interface BinlogInfoService {
	public void setBaseDir(String baseDir);

	public BinlogInfo getBinlogInfo(String clientName);

	public void saveBinlogInfo(String clientName, BinlogInfo binlogInfo);

	public void removeBinlogInfo(String clientName);

	public List<String> findSyncTaskClientNames();
}
