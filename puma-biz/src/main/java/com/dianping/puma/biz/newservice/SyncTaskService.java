package com.dianping.puma.biz.newservice;

import com.dianping.puma.biz.entity.sync.SyncTaskEntity;

import java.util.List;

public interface SyncTaskService {

	List<SyncTaskEntity> findBySyncServerHost(String host);

	void create(SyncTaskEntity entity);

	void delete(SyncTaskEntity entity);
}
