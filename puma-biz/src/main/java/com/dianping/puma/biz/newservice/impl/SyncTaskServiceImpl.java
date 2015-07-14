package com.dianping.puma.biz.newservice.impl;

import com.dianping.puma.biz.dao.SyncTaskDao;
import com.dianping.puma.biz.dao.SyncTaskDstDbDao;
import com.dianping.puma.biz.entity.SyncTaskEntity;
import com.dianping.puma.biz.newservice.SyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

public class SyncTaskServiceImpl implements SyncTaskService {

	@Autowired
	SyncTaskDao syncTaskDao;

	@Autowired
	SyncTaskDstDbDao syncTaskDstDbDao;

	@Override
	public List<SyncTaskEntity> findBySyncServerHost(String host) {
		return null;
	}

	@Override
	public void create(SyncTaskEntity task) {

	}

	@Override
	public void delete(SyncTaskEntity entity) {

	}
}
