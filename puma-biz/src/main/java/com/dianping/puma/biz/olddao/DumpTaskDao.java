package com.dianping.puma.biz.olddao;

import com.dianping.puma.biz.entity.old.DumpTask;

import java.util.List;

public interface DumpTaskDao {

	DumpTask find(long id);
	
	DumpTask find(String name);

	List<DumpTask> findAll();

	void create(DumpTask dumpTask);

	void remove(String name);
	
	void remove(long id);
}
