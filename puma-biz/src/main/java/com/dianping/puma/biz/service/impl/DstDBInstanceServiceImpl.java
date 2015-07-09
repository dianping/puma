package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.olddao.DstDBInstanceDao;
import com.dianping.puma.biz.entity.old.DstDBInstance;
import com.dianping.puma.biz.service.DstDBInstanceService;

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
	public DstDBInstance find(long id) {
		return dstDbInstanceDao.find(id);
	}

	
	@Override
	public List<DstDBInstance> findAll() {
		return dstDbInstanceDao.findAll();
	}
	
	@Override
	public long count() {
		return dstDbInstanceDao.count();
	}
	
	@Override
	public List<DstDBInstance> findByPage(int page, int pageSize) {
		return dstDbInstanceDao.findByPage(page, pageSize);
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
	
	@Override
	public void remove(long id) {
		dstDbInstanceDao.remove(id);
	}
}
