package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.dao.SrcDBInstanceDao;
import com.dianping.puma.core.entity.SrcDBInstanceEntity;
import com.dianping.puma.core.service.SrcDBInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("srcDBInstanceService")
public class SrcDBInstanceServiceImpl implements SrcDBInstanceService {

	@Autowired
	SrcDBInstanceDao srcDbInstanceDao;

	@Override
	public SrcDBInstanceEntity find(String id) {
		return srcDbInstanceDao.find(id);
	}

	@Override
	public List<SrcDBInstanceEntity> findAll() {
		return srcDbInstanceDao.findAll();
	}

	@Override
	public void create(SrcDBInstanceEntity entity) {
		srcDbInstanceDao.create(entity);
	}

	@Override
	public void update(SrcDBInstanceEntity entity) {
		srcDbInstanceDao.update(entity);
	}

	@Override
	public void remove(String id) {
		srcDbInstanceDao.remove(id);
	}
}
