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
	public DstDBInstance find(String name) {
		return dstDbInstanceDao.find(name);
	}

	@Override
	public List<DstDBInstance> findAll() {
		return dstDbInstanceDao.findAll();
	}

	@Override
	public void create(DstDBInstance dstDBInstance) {
		dstDbInstanceDao.create(dstDBInstance);
	}

	@Override
	public void update(DstDBInstance dstDBInstance) {
		dstDbInstanceDao.update(dstDBInstance);
	}

	@Override
	public void remove(String name) {
		dstDbInstanceDao.remove(name);
	}
}
