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
	public PumaServer find(String id) {
		return pumaServerDao.find(id);
	}

	@Override
	public PumaServer findByName(String name) {
		return pumaServerDao.findByName(name);
	}

	public PumaServer findByHostAndPort(String host, Integer port) {
		return pumaServerDao.findByHostAndPort(host, port);
	}

	@Override
	public List<PumaServer> findAll() {
		return pumaServerDao.findAll();
	}

	@Override
	public void create(PumaServer entity) {
		pumaServerDao.create(entity);
	}

	@Override
	public void update(PumaServer entity) {
		pumaServerDao.update(entity);
	}

	@Override
	public void remove(String id) {
		pumaServerDao.remove(id);
	}
}
