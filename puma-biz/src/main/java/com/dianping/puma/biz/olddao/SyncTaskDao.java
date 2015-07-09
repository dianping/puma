package com.dianping.puma.biz.olddao;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.biz.entity.old.SyncTask;

import java.util.List;

public interface SyncTaskDao {
	
	SyncTask find(long id);

	SyncTask find(String name);

	List<SyncTask> findBySyncServerName(String syncServerName);

	List<SyncTask> findByDstDBInstanceName(String dstDBInstanceName);

	List<SyncTask> findByPumaServerName(String pumaServerName);
	
	List<SyncTask> findByPumaTaskName(String pumaTaskName);

	List<SyncTask> findAll();

	long count();

	List<SyncTask> findByPage(int page, int pageSize);
	
	void create(SyncTask syncTask);

	void remove(String name);
	
	void remove(long id);
	
	void update(SyncTask syncTask);
	
	List<SyncTask> find(int offset, int limit);
	
	void updateStatusAction(String name,ActionController controller);
}
