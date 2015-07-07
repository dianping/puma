package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.SyncServer;

import java.util.List;

public interface SyncServerService {
	
	SyncServer find(long id);

	SyncServer find(String name);

	List<SyncServer> findAll();

	long count();

	List<SyncServer> findByPage(int page, int pageSize);
	
	void create(SyncServer syncServer);

	void update(SyncServer syncServer);

	void remove(String name);
	
	void remove(long id);

	SyncServer findByHost(String host);
}
