package com.dianping.puma.biz.service;

import java.util.List;

import com.dianping.puma.biz.entity.PumaServerEntity;

public interface PumaServerService {

	PumaServerEntity findById(int id);

	PumaServerEntity find(String name);

	PumaServerEntity findByHost(String host);

	List<PumaServerEntity> findOnCurrentServer();

	List<PumaServerEntity> findByTaskId(int taskId);

	List<PumaServerEntity> findByDatabaseAndTables(String database, List<String> tables);

	List<PumaServerEntity> findAll();

	long count();

	List<PumaServerEntity> findByPage(int page, int pageSize);

	void registerByHost(String host);

	void create(PumaServerEntity pumaServer);

	void update(PumaServerEntity pumaServer);

	void remove(String name);

	void remove(int id);
}
