package com.dianping.puma.admin.container;

import com.dianping.puma.core.model.PumaTaskState;
import com.dianping.puma.core.monitor.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.PumaTaskStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service("pumaTaskStateContainer")
public class PumaTaskStateContainer implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(PumaTaskStateContainer.class);

	private ConcurrentHashMap<String, PumaTaskState> stateMap = new ConcurrentHashMap<String, PumaTaskState>();

	public PumaTaskState get(String taskId) {
		return stateMap.get(taskId);
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

	@Override
	public void onEvent(Event event) {
		LOG.info("Receive event.");

		if (event instanceof PumaTaskStateEvent) {
			LOG.info("Receive puma task state event.");

			PumaTaskStateEvent pumaTaskStateEvent = (PumaTaskStateEvent) event;
			List<String> taskIds = pumaTaskStateEvent.getTaskIds();
			List<PumaTaskState> states = pumaTaskStateEvent.getStates();

			for (int i = 0; i != taskIds.size() && i != states.size(); ++i) {
				this.update(taskIds.get(i), states.get(i));
			}
		}
	}
}
