package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.dao.PumaServerDao;
import com.dianping.puma.core.entity.PumaServer;
import com.dianping.puma.core.service.PumaServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pumaServerService")
public class PumaServerServiceImpl implements PumaServerService {

	@Autowired
	PumaServerDao pumaServerDao;

	@Override
	public PumaServer find(String name) {
		return pumaServerDao.find(name);
	}

	@Override
	public PumaServer find(long id) {
		return pumaServerDao.find(id);
	}

	public PumaServer findByHost(String host) {
		return pumaServerDao.findByHost(host);
	}

	@Override
	public List<PumaServer> findAll() {
		return pumaServerDao.findAll();
	}

	@Override
	public long count() {
		return pumaServerDao.count();
	}
	
	@Override
	public List<PumaServer> findByPage(int page, int pageSize) {
		return pumaServerDao.findByPage(page, pageSize);
	}

	@Override
	public void create(PumaServer pumaServer) {
		pumaServerDao.create(pumaServer);
	}

	@Override
	public void update(PumaServer pumaServer) {
		pumaServerDao.update(pumaServer);
	}

	@Override
	public void remove(String name) {
		pumaServerDao.remove(name);
	}

	@Override
	public void remove(long id) {
		pumaServerDao.remove(id);
	}
}
