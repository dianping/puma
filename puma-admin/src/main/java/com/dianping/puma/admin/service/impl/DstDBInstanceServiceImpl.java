package com.dianping.puma.admin.service.impl;

import com.dianping.puma.admin.service.DstDBInstanceService;
import com.dianping.puma.core.dao.DstDBInstanceDao;
import com.dianping.puma.core.entity.DstDBInstanceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dstDBInstanceService")
public class DstDBInstanceServiceImpl implements DstDBInstanceService {

	@Autowired
	DstDBInstanceDao dstDbInstanceDao;

	@Override
	public DstDBInstanceEntity find(String id) {
		return dstDbInstanceDao.find(id);
	}

	@Override
	public List<DstDBInstanceEntity> findAll() {
		return dstDbInstanceDao.findAll();
	}

	@Override
	public void create(DstDBInstanceEntity entity) {
		dstDbInstanceDao.create(entity);
	}

	@Override
	public void update(DstDBInstanceEntity entity) {
		dstDbInstanceDao.update(entity);
	}

	@Override
	public void remove(String id) {
		dstDbInstanceDao.remove(id);
	}
}
