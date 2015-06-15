package com.dianping.puma.remote.reporter;

import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.config.PumaServerConfig;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.event.PumaTaskStateEvent;
import com.dianping.puma.core.service.PumaTaskStateService;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("pumaTaskStateReporter")
public class PumaTaskStateReporter {

	@Autowired
	SwallowEventPublisher pumaTaskStatePublisher;

	@Autowired
	PumaServerConfig pumaServerConfig;

	@Autowired
	PumaTaskStateService pumaTaskStateService;

	@Scheduled(cron = "0/5 * * * * ?")
	public void report() throws SendFailedException {
		PumaTaskStateEvent event = new PumaTaskStateEvent();
		List<String> serverNames = new ArrayList<String>();
		serverNames.add(pumaServerConfig.getName());
		event.setServerNames(serverNames);
		event.setTaskStates(pumaTaskStateService.findAll());
		pumaTaskStatePublisher.publish(event);
	}
}
