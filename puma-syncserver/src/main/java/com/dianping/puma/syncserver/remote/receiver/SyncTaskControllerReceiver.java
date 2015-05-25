package com.dianping.puma.syncserver.remote.receiver;

import com.dianping.cat.Cat;
import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.monitor.event.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.event.SyncTaskControllerEvent;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
import com.dianping.puma.syncserver.job.container.exception.TECException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("syncTaskControllerReceiver")
public class SyncTaskControllerReceiver implements EventListener {

	private static final Logger LOG = LoggerFactory.getLogger(SyncTaskControllerEvent.class);

	@Autowired
	TaskExecutorContainer taskExecutorContainer;

	@Override
	public void onEvent(Event event) {
		try {
			if (event instanceof SyncTaskControllerEvent) {
				LOG.info("Receiving sync task controller event({}).", event.toString());

				String name = ((SyncTaskControllerEvent) event).getTaskName();
				ActionController controller = ((SyncTaskControllerEvent) event).getController();

				switch (controller) {
				case START:
					startSyncTask(name);
					break;
				case STOP:
					stopSyncTask(name);
					break;
				}
			}
		} catch (TECException e) {
			LOG.error("Receiving sync task controller event({}) error.", event.toString(), e);
			Cat.logError(e);
		}
	}

	private void startSyncTask(String name) {
		LOG.info("Starting sync task({})...", name);

		taskExecutorContainer.start(name);
	}

	private void stopSyncTask(String name) {
		LOG.info("Stopping sync task({})...", name);

		taskExecutorContainer.stop(name);
	}
}
