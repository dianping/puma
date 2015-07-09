package com.dianping.puma.biz.olddao;

import com.dianping.puma.biz.entity.old.SyncServer;

import java.util.List;

public interface SyncServerDao {
	
	SyncServer find(long id);

	SyncServer find(String name);

	SyncServer findByHost(String host);
	
	List<SyncServer> findAll();

	long count();

	List<SyncServer> findByPage(int page, int pageSize);
	
	void create(SyncServer syncServer);

	void update(SyncServer syncServer);

	void remove(String name);
	
	void remove(long id);
}
