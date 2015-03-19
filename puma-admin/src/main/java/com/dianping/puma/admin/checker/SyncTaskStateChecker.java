package com.dianping.puma.admin.checker;

import com.dianping.puma.core.container.SyncTaskStateContainer;
import com.dianping.puma.core.monitor.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.SyncTaskStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("syncTaskStateChecker")
public class SyncTaskStateChecker implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(SyncTaskStateChecker.class);

	@Autowired
	SyncTaskStateContainer syncTaskStateContainer;

	@Override
	public void onEvent(Event event) {
		LOG.info("Receive sync task state event.");

		try {
			SyncTaskStateEvent syncTaskStateEvent = (SyncTaskStateEvent) event;
			syncTaskStateContainer.updateAll(syncTaskStateEvent.getSyncTaskStateMap());
		} catch (Exception e) {
			LOG.error("Receive puma task state event error: {}.", e.getMessage());
		}
	}
}
