package com.dianping.puma.core.container;

import com.dianping.puma.core.model.PumaTaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service("pumaTaskStateContainer")
public class PumaTaskStateContainer {

	private static final Logger LOG = LoggerFactory.getLogger(PumaTaskStateContainer.class);

	private ConcurrentHashMap<String, PumaTaskState> stateMap = new ConcurrentHashMap<String, PumaTaskState>();

	public PumaTaskState get(String taskId) {
		return stateMap.get(taskId);
	}

	public List<String> getAllTaskIds() {
		return new ArrayList<String>(stateMap.keySet());
	}

	public List<PumaTaskState> getAll() {
		return new ArrayList<PumaTaskState>(stateMap.values());
	}

	public void add(String taskId, PumaTaskState state) {
		stateMap.put(taskId, state);
	}

	public void create(String taskId) {
		PumaTaskState state = new PumaTaskState();
		this.add(taskId, state);
	}

	public void update(String taskId, PumaTaskState state) {
		stateMap.replace(taskId, state);
	}

	public void remove(String taskId) {
		stateMap.remove(taskId);
	}
}
