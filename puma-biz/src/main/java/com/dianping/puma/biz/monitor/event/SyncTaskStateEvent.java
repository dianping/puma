package com.dianping.puma.biz.monitor.event;

import com.dianping.puma.core.model.state.SyncTaskState;

import java.util.Map;

public class SyncTaskStateEvent extends TaskStateEvent<SyncTaskState> {

	Map<String, SyncTaskState> syncTaskStateMap;

	public Map<String, SyncTaskState> getSyncTaskStateMap() {
		return syncTaskStateMap;
	}

	public void setSyncTaskStateMap(Map<String, SyncTaskState> syncTaskStateMap) {
		this.syncTaskStateMap = syncTaskStateMap;
	}
}
