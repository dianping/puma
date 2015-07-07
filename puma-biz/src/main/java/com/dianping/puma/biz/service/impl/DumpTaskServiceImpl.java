package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.service.DumpTaskService;
import com.dianping.puma.biz.dao.DumpTaskDao;
import com.dianping.puma.biz.entity.DumpTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dumpTaskService")
public class DumpTaskServiceImpl implements DumpTaskService {

	@Autowired
	DumpTaskDao dumpTaskDao;

	public DumpTask find(String name) {
		return dumpTaskDao.find(name);
	}
	
	public DumpTask find(long id) {
		return dumpTaskDao.find(id);
	}

	public List<DumpTask> findAll() {
		return dumpTaskDao.findAll();
	}

	public void create(DumpTask dumpTask) {
		dumpTaskDao.create(dumpTask);
	}

	public void remove(String name) {
		dumpTaskDao.remove(name);
	}
	
	public void remove(long id) {
		dumpTaskDao.remove(id);
	}
}
