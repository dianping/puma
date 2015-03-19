package com.dianping.puma.syncserver.reporter;

import com.dianping.puma.core.container.SyncTaskStateContainer;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.SyncTaskStateEvent;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("syncTaskStateReporter")
public class SyncTaskStateReporter {

	@Autowired
	SwallowEventPublisher syncTaskStatePublisher;

	@Autowired
	SyncTaskStateContainer syncTaskStateContainer;

	@Autowired
	Config config;

	@Scheduled(cron = "0/5 * * * * ?")
	public void report() throws SendFailedException {
		SyncTaskStateEvent event = new SyncTaskStateEvent();
		event.setSyncServerName(config.getSyncServerName());
		event.setSyncTaskStateMap(syncTaskStateContainer.getAll());

		syncTaskStatePublisher.publish(event);
	}
}
