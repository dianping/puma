package com.dianping.puma.admin.service.impl;

import com.dianping.puma.admin.service.PumaServerService;
import com.dianping.puma.core.dao.PumaServerDao;
import com.dianping.puma.core.entity.PumaServerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pumaServerService")
public class PumaServerServiceImpl implements PumaServerService {

	@Autowired
	PumaServerDao pumaServerDao;

	@Override
	public PumaServerEntity find(String id) {
		return pumaServerDao.find(id);
	}

	@Override
	public List<PumaServerEntity> findAll() {
		return pumaServerDao.findAll();
	}

	@Override
	public void create(PumaServerEntity entity) {
		pumaServerDao.create(entity);
	}

	@Override
	public void update(PumaServerEntity entity) {
		pumaServerDao.update(entity);
	}

	@Override
	public void remove(String id) {
		pumaServerDao.remove(id);
	}
}
