package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.dao.SyncServerDao;
import com.dianping.puma.core.entity.SyncServer;
import com.dianping.puma.core.service.SyncServerService;
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
	public List<SyncServer> findAll() {
		return syncServerDao.findAll();
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
	public SyncServer findByHost(String host, int port){
		return syncServerDao.findByHost(host, port);
	}
}
