package com.dianping.puma.admin.remote.receiver;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.model.state.SyncTaskState;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.monitor.event.SyncTaskStateEvent;
import com.dianping.puma.core.service.SyncTaskService;
import com.dianping.puma.core.service.SyncTaskStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service("syncTaskStateReceiver")
public class SyncTaskStateReceiver implements EventListener {
	private static final Logger LOG = LoggerFactory.getLogger(SyncTaskStateReceiver.class);

	@Autowired
	SyncTaskStateService syncTaskStateService;

	@Autowired
	SyncTaskService syncTaskService;

	@PostConstruct
	public void init() {
		List<SyncTask> syncTasks = syncTaskService.findAll();
		for (SyncTask syncTask: syncTasks) {
			SyncTaskState syncTaskState = new SyncTaskState();
			syncTaskState.setTaskName(syncTask.getName());
			syncTaskState.setStatus(Status.PREPARING);
			syncTaskStateService.add(syncTaskState);
		}
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof SyncTaskStateEvent) {
			LOG.info("Receive sync task state event.");

			List<SyncTaskState> syncTaskStates = ((SyncTaskStateEvent) event).getTaskStates();
			for (SyncTaskState syncTaskState: syncTaskStates) {
				syncTaskStateService.add(syncTaskState);
			}
		}
	}
}
