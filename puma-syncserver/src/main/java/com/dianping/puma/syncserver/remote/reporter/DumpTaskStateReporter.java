package com.dianping.puma.syncserver.remote.reporter;

import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.event.DumpTaskStateEvent;
import com.dianping.puma.core.service.DumpTaskStateService;
import com.dianping.puma.syncserver.config.SyncServerConfig;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("dumpTaskStateReporter")
public class DumpTaskStateReporter {

	@Autowired
	SwallowEventPublisher dumpTaskStatePublisher;

	@Autowired
	DumpTaskStateService dumpTaskStateService;

	@Autowired
	SyncServerConfig syncServerConfig;

	@Scheduled(cron = "0/5 * * * * ?")
	public void report() throws SendFailedException {
		DumpTaskStateEvent event = new DumpTaskStateEvent();
		event.setServerName(syncServerConfig.getSyncServerName());
		event.setTaskStates(dumpTaskStateService.findAll());
		dumpTaskStatePublisher.publish(event);
	}
}
