package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.dao.DumpTaskDao;
import com.dianping.puma.core.entity.DumpTask;
import com.dianping.puma.core.service.DumpTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dumpTaskService2")
public class DumpTaskServiceImpl implements DumpTaskService {

	@Autowired
	DumpTaskDao dumpTaskDao2;

	public DumpTask find(String name) {
		return dumpTaskDao2.find(name);
	}

	public List<DumpTask> findAll() {
		return dumpTaskDao2.findAll();
	}

	public void create(DumpTask dumpTask) {
		dumpTaskDao2.create(dumpTask);
	}
}
