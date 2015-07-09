package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.service.PumaTaskService;
import com.dianping.puma.biz.olddao.PumaTaskDao;
import com.dianping.puma.biz.entity.old.PumaTask;

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
	public PumaTask find(long id) {
		return pumaTaskDao.find(id);
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
	public List<PumaTask> findByPumaServerNames(String pumaServerName){
		return pumaTaskDao.findByPumaServerNames(pumaServerName);
	}
	
	
	@Override
	public List<PumaTask> findAll() {
		return pumaTaskDao.findAll();
	}

	@Override
	public long count() {
		return pumaTaskDao.count();
	}
	
	@Override
	public List<PumaTask> findByPage(int page, int pageSize) {
		return pumaTaskDao.findByPage(page, pageSize);
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
	
	@Override
	public void remove(long id) {
		pumaTaskDao.remove(id);
	}
}
