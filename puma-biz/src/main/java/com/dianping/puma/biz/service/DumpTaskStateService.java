package com.dianping.puma.biz.service;

import com.dianping.puma.core.model.state.DumpTaskState;

import java.util.List;

public interface DumpTaskStateService {
	
	DumpTaskState find(String taskName);

	List<DumpTaskState> findAll();

	void add(DumpTaskState taskState);

	void addAll(List<DumpTaskState> taskStates);

	void remove(String taskName);

	void removeAll();
}
