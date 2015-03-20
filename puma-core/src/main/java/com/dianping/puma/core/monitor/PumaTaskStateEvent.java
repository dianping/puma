package com.dianping.puma.core.monitor;

import com.dianping.puma.core.model.PumaTaskState;

import java.util.List;
import java.util.Map;

public class PumaTaskStateEvent extends PumaTaskEvent {

	private Map<String, PumaTaskState> stateMap;

	public Map<String, PumaTaskState> getStateMap() {
		return stateMap;
	}

	public void setStateMap(Map<String, PumaTaskState> stateMap) {
		this.stateMap = stateMap;
	}
}
