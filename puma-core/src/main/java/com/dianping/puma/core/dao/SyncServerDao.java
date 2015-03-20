package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.SyncServer;

import java.util.List;

public interface SyncServerDao {

	SyncServer find(String id);

	SyncServer findByHost(String host,int port);
	
	List<SyncServer> findAll();

	void create(SyncServer entity);

	void update(SyncServer entity);

	void remove(String id);
}
