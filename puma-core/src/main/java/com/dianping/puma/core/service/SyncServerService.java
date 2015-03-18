package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.SyncServer;

import java.util.List;

public interface SyncServerService {

	SyncServer find(String id);

	List<SyncServer> findAll();

	void create(SyncServer entity);

	void update(SyncServer entity);

	void remove(String id);

	SyncServer findByHost(String host, int port);
}
