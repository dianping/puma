package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.dao.SyncTaskDao;
import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.service.SyncTaskService;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

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

	public List<SyncTask> findBySyncServerName(String syncServerName) {
		return syncTaskDao.findBySyncServerName(syncServerName);
	}

	public List<SyncTask> findAll() {
		return syncTaskDao.findAll();
	}

	public void create(SyncTask syncTask) {
		syncTaskDao.create(syncTask);
	}
	
	public List<SyncTask> find(int offset, int limit) {
		return syncTaskDao.find(offset,limit);
	}
}
