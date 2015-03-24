package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.dao.SrcDBInstanceDao;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.service.SrcDBInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("srcDBInstanceService")
public class SrcDBInstanceServiceImpl implements SrcDBInstanceService {

	@Autowired
	SrcDBInstanceDao srcDbInstanceDao;

	@Override
	public SrcDBInstance find(String name) {
		return srcDbInstanceDao.find(name);
	}

	@Override
	public List<SrcDBInstance> findAll() {
		return srcDbInstanceDao.findAll();
	}

	@Override
	public void create(SrcDBInstance srcDBInstance) {
		srcDbInstanceDao.create(srcDBInstance);
	}

	@Override
	public void update(SrcDBInstance srcDBInstance) {
		srcDbInstanceDao.update(srcDBInstance);
	}

	@Override
	public void remove(String name) {
		srcDbInstanceDao.remove(name);
	}
}
