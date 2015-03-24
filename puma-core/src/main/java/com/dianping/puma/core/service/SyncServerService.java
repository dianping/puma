package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.SyncServer;

import java.util.List;

public interface SyncServerService {

	SyncServer find(String name);

	List<SyncServer> findAll();

	void create(SyncServer syncServer);

	void update(SyncServer syncServer);

	void remove(String id);

	SyncServer findByHost(String host, int port);
}
