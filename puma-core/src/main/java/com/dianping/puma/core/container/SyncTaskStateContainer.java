package com.dianping.puma.core.container;

import com.dianping.puma.core.model.SyncTaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service("syncTaskStateContainer")
public class SyncTaskStateContainer {

	private static final Logger LOG = LoggerFactory.getLogger(SyncTaskStateContainer.class);

	private ConcurrentMap<String, SyncTaskState> stateMap = new ConcurrentHashMap<String, SyncTaskState>();

	public void create(String taskName) {
		SyncTaskState state = new SyncTaskState();
		this.stateMap.put(taskName, state);
	}

	public void add(String taskName, SyncTaskState state) {
		this.stateMap.put(taskName, state);
	}

	public void addAll(Map<String, SyncTaskState> stateMap) {
		this.stateMap.putAll(stateMap);
	}

	public SyncTaskState get(String taskName) {
		return this.stateMap.get(taskName);
	}

	public final Map<String, SyncTaskState> getAll() {
		return this.stateMap;
	}

	public void update(String taskName, SyncTaskState state) {
		this.stateMap.put(taskName, state);
	}

	public void updateAll(Map<String, SyncTaskState> stateMap) {
		this.stateMap.putAll(stateMap);
	}

	public void remove(String taskName) {
		this.stateMap.remove(taskName);
	}
}
