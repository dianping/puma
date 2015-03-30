package com.dianping.puma.core.dao;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.entity.SyncTask;

import java.util.List;

public interface SyncTaskDao {
	
	SyncTask find(long id);

	SyncTask find(String name);

	List<SyncTask> findBySyncServerName(String syncServerName);

	List<SyncTask> findByDstDBInstanceName(String dstDBInstanceName);

	List<SyncTask> findByPumaServerName(String pumaServerName);

	List<SyncTask> findAll();

	void create(SyncTask syncTask);

	void remove(String name);
	
	void remove(long id);
	
	List<SyncTask> find(int offset, int limit);
	
	void updateStatusAction(String name,ActionController controller);
}
