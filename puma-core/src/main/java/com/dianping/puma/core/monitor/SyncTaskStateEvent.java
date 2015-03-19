package com.dianping.puma.core.monitor;

import com.dianping.puma.core.model.SyncTaskState;

import java.util.Map;

public class SyncTaskStateEvent extends SyncTaskEvent {

	Map<String, SyncTaskState> syncTaskStateMap;

	public Map<String, SyncTaskState> getSyncTaskStateMap() {
		return syncTaskStateMap;
	}

	public void setSyncTaskStateMap(Map<String, SyncTaskState> syncTaskStateMap) {
		this.syncTaskStateMap = syncTaskStateMap;
	}
}
