package com.dianping.puma.storage.manage;

import com.dianping.puma.core.model.BinlogInfo;

public interface InstanceStorageManager {

	BinlogInfo getBinlogInfo(String taskName);

	void setBinlogInfo(String taskName, BinlogInfo binlogInfo);

	void rename(String oriTaskName, String taskName);

	void remove(String taskName);

}
