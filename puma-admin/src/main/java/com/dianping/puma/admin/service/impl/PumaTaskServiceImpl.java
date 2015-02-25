package com.dianping.puma.admin.service.impl;

import com.dianping.puma.admin.service.PumaTaskService;
import com.dianping.puma.core.dao.PumaTaskDao;
import com.dianping.puma.core.entity.PumaTaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pumaTaskService")
public class PumaTaskServiceImpl implements PumaTaskService {

	@Autowired
	PumaTaskDao pumaTaskDao;

	@Override
	public PumaTaskEntity find(String id) {
		return pumaTaskDao.find(id);
	}

	@Override
	public List<PumaTaskEntity> findAll() {
		return pumaTaskDao.findAll();
	}

	@Override
	public void create(PumaTaskEntity entity) {
		pumaTaskDao.create(entity);
	}

	@Override
	public void update(PumaTaskEntity entity) {
		pumaTaskDao.update(entity);
	}

	@Override
	public void remove(String id) {
		pumaTaskDao.remove(id);
	}
}
