package com.dianping.puma.biz.service;

import com.dianping.puma.core.model.state.ShardSyncTaskState;
import com.dianping.puma.core.model.state.SyncTaskState;

import java.util.List;

public interface ShardSyncTaskStateService {

	void add(ShardSyncTaskState taskState);

	void addAll(List<ShardSyncTaskState> taskStates);

	ShardSyncTaskState find(String taskName);

	List<ShardSyncTaskState> findAll();

	void remove(String taskName);

	void removeAll();
}
