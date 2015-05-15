package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.dao.SyncTaskDao;
import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.service.SyncTaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("syncTaskService")
public class SyncTaskServiceImpl implements SyncTaskService {

	@Autowired
	SyncTaskDao syncTaskDao;

	public SyncTask find(String name) {
		return syncTaskDao.find(name);
	}
	
	public SyncTask find(long id) {
		return syncTaskDao.find(id);
	}

	public List<SyncTask> findBySyncServerName(String syncServerName) {
		return syncTaskDao.findBySyncServerName(syncServerName);
	}

	public List<SyncTask> findByDstDBInstanceName(String dstDBInstanceName) {
		return syncTaskDao.findByDstDBInstanceName(dstDBInstanceName);
	}

	public List<SyncTask> findByPumaServerName(String pumaServerName) {
		return syncTaskDao.findByPumaServerName(pumaServerName);
	}

	public List<SyncTask> findAll() {
		return syncTaskDao.findAll();
	}

	@Override
	public long count() {
		return syncTaskDao.count();
	}
	
	@Override
	public List<SyncTask> findByPage(int page, int pageSize) {
		return syncTaskDao.findByPage(page, pageSize);
	}
	
	
	public void create(SyncTask syncTask) {
		syncTaskDao.create(syncTask);
	}

	public void remove(String name) {
		syncTaskDao.remove(name);
	}
	
	public void remove(long id) {
		syncTaskDao.remove(id);
	}
	
	public List<SyncTask> find(int offset, int limit) {
		return syncTaskDao.find(offset,limit);
	}
	
	public void updateStatusAction(String name,ActionController controller){
		syncTaskDao.updateStatusAction(name, controller);
	}
}
