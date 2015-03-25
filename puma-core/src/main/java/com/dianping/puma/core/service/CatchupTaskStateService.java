package com.dianping.puma.core.service;

import com.dianping.puma.core.model.state.CatchupTaskState;

import java.util.List;

public interface CatchupTaskStateService {

	CatchupTaskState find(String taskName);

	List<CatchupTaskState> findAll();

	void add(CatchupTaskState taskState);

	void addAll(List<CatchupTaskState> taskStates);

	void remove(String taskName);

	void removeAll();
}
