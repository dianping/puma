package com.dianping.puma.core.monitor;

import com.dianping.puma.core.model.PumaTaskState;

import java.util.List;
import java.util.Map;

public class PumaTaskStateEvent extends PumaTaskEvent {

	private List<String> taskIds;

	private List<PumaTaskState> states;

	private Map<String, PumaTaskState> stateMap;

	public Map<String, PumaTaskState> getStateMap() {
		return stateMap;
	}

	public void setStateMap(Map<String, PumaTaskState> stateMap) {
		this.stateMap = stateMap;
	}

	public List<String> getTaskIds() {
		return taskIds;
	}

	public void setTaskIds(List<String> taskIds) {
		this.taskIds = taskIds;
	}

	public List<PumaTaskState> getStates() {
		return states;
	}

	public void setStates(List<PumaTaskState> states) {
		this.states = states;
	}
}
