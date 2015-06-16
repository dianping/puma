package com.dianping.puma.core.service;

import com.dianping.puma.core.model.state.PumaTaskState;

import java.util.List;

public interface PumaTaskStateService {

	void add(PumaTaskState taskState);

	void addAll(List<PumaTaskState> taskStates);

	PumaTaskState find(String name);

	List<PumaTaskState> findAll();

	void remove(String name);

	void removeAll();
	
	String getTaskStateName(String taskName, String serverName);
	
}
