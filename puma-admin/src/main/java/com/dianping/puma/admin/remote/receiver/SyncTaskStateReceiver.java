package com.dianping.puma.admin.remote.receiver;

import com.dianping.puma.biz.entity.SyncTaskState;
import com.dianping.puma.biz.entity.old.SyncTask;
import com.dianping.puma.biz.event.EventListener;
import com.dianping.puma.biz.event.entity.Event;
import com.dianping.puma.biz.service.SyncTaskService;
import com.dianping.puma.biz.service.SyncTaskStateService;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.state.SyncTaskState;
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
//		if (event instanceof SyncTaskStateEvent) {
//			LOG.info("Receive sync task state event.");
//
//			List<SyncTaskState> syncTaskStates = ((SyncTaskStateEvent) event).getTaskStates();
//			for (SyncTaskState syncTaskState: syncTaskStates) {
//				syncTaskStateService.add(syncTaskState);
//			}
//		}
	}
}
