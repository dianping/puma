package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.old.DumpTask;

import java.util.List;

public interface DumpTaskService {
	
	DumpTask find(long id);

	DumpTask find(String name);

	List<DumpTask> findAll();

	void create(DumpTask dumpTask);

	void remove(String name);
	
	void remove(long id);
}
