package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.SyncServer;

import java.util.List;

public interface SyncServerDao {

	SyncServer find(String name);

	SyncServer findByHost(String host);
	
	List<SyncServer> findAll();

	void create(SyncServer syncServer);

	void update(SyncServer syncServer);

	void remove(String name);
}
