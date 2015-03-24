package com.dianping.puma.core.service;

import com.dianping.puma.core.model.state.SyncTaskState;

import java.util.List;

public interface SyncTaskStateService {

	void add(SyncTaskState taskState);

	void addAll(List<SyncTaskState> taskStates);

	SyncTaskState find(String taskName);

	List<SyncTaskState> findAll();

	void remove(String taskName);

	void removeAll();
}
