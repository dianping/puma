package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.SyncServerDao;
import com.dianping.puma.biz.entity.SyncServerEntity;
import com.dianping.puma.biz.service.SyncServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncServerServiceImpl implements SyncServerService {

	@Autowired
	SyncServerDao syncServerDao;

	@Override
	public SyncServerEntity find(int id) {
		return syncServerDao.find(id);
	}

	@Override
	public List<SyncServerEntity> findAll() {
		return syncServerDao.findAll();
	}

	@Override
	public int create(SyncServerEntity entity) {
		return syncServerDao.insert(entity);
	}

	@Override
	public int update(SyncServerEntity entity) {
		return syncServerDao.update(entity);
	}

	@Override
	public int createOrUpdate(SyncServerEntity entity) {
		return find(entity.getId()) == null ? create(entity) : update(entity);
	}

	@Override
	public int remove(int id) {
		return syncServerDao.delete(id);
	}
}
