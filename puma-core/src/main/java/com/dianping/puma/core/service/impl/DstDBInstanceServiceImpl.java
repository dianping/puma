package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.dao.DstDBInstanceDao;
import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.service.DstDBInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dstDBInstanceService")
public class DstDBInstanceServiceImpl implements DstDBInstanceService {

	@Autowired
	DstDBInstanceDao dstDbInstanceDao;

	@Override
	public DstDBInstance find(String id) {
		return dstDbInstanceDao.find(id);
	}

	@Override
	public DstDBInstance findByName(String name) {
		return dstDbInstanceDao.findByName(name);
	}

	@Override
	public List<DstDBInstance> findAll() {
		return dstDbInstanceDao.findAll();
	}

	@Override
	public void create(DstDBInstance entity) {
		dstDbInstanceDao.create(entity);
	}

	@Override
	public void update(DstDBInstance entity) {
		dstDbInstanceDao.update(entity);
	}

	@Override
	public void remove(String id) {
		dstDbInstanceDao.remove(id);
	}
}
