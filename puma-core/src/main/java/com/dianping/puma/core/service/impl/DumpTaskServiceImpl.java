package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.dao.DumpTaskDao;
import com.dianping.puma.core.entity.DumpTask;
import com.dianping.puma.core.service.DumpTaskService;
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

	public List<DumpTask> findAll() {
		return dumpTaskDao.findAll();
	}

	public void create(DumpTask dumpTask) {
		dumpTaskDao.create(dumpTask);
	}
}
