package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.service.SyncServerService;
import com.dianping.puma.biz.dao.SyncServerDao;
import com.dianping.puma.biz.entity.SyncServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("syncServerService")
public class SyncServerServiceImpl implements SyncServerService {

	@Autowired
	SyncServerDao syncServerDao;

	@Override
	public SyncServer find(String name) {
		return syncServerDao.find(name);
	}
	
	@Override
	public SyncServer find(long id) {
		return syncServerDao.find(id);
	}

	@Override
	public List<SyncServer> findAll() {
		return syncServerDao.findAll();
	}

	@Override
	public long count() {
		return syncServerDao.count();
	}
	
	@Override
	public List<SyncServer> findByPage(int page, int pageSize) {
		return syncServerDao.findByPage(page, pageSize);
	}
	
	@Override
	public void create(SyncServer syncServer) {
		syncServerDao.create(syncServer);
	}

	@Override
	public void update(SyncServer syncServer) {
		syncServerDao.update(syncServer);
	}

	@Override
	public void remove(String name) {
		syncServerDao.remove(name);
	}
	
	@Override
	public void remove(long id) {
		syncServerDao.remove(id);
	}
	
	@Override
	public SyncServer findByHost(String host){
		return syncServerDao.findByHost(host);
	}
}
