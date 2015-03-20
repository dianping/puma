package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.DumpTask;

import java.util.List;

public interface DumpTaskDao {

	DumpTask find(String name);

	List<DumpTask> findAll();

	void create(DumpTask dumpTask);

	void remove(String name);
}
