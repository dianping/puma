package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.dao.PumaTaskDao;
import com.dianping.puma.core.entity.PumaTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pumaTaskService")
public class PumaTaskServiceImpl implements PumaTaskService {

	@Autowired
	PumaTaskDao pumaTaskDao;

	@Override
	public PumaTask find(String name) {
		return pumaTaskDao.find(name);
	}

	@Override
	public List<PumaTask> findBySrcDBInstanceName(String srcDBInstanceName) {
		return pumaTaskDao.findBySrcDBInstanceName(srcDBInstanceName);
	}

	@Override
	public List<PumaTask> findByPumaServerName(String pumaServerName) {
		return pumaTaskDao.findByPumaServerName(pumaServerName);
	}

	@Override
	public List<PumaTask> findAll() {
		return pumaTaskDao.findAll();
	}

	@Override
	public void create(PumaTask pumaTask) {
		pumaTaskDao.create(pumaTask);
	}

	@Override
	public void update(PumaTask pumaTask) {
		pumaTaskDao.update(pumaTask);
	}

	@Override
	public void remove(String name) {
		pumaTaskDao.remove(name);
	}
}
