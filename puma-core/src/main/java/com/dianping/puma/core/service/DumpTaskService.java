package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.DumpTask;

import java.util.List;

public interface DumpTaskService {

	DumpTask find(String name);

	List<DumpTask> findAll();

	void create(DumpTask dumpTask);

	void remove(String name);
}
