package com.dianping.puma.remote.reporter;

import com.dianping.puma.config.PumaServerConfig;
import com.dianping.puma.biz.monitor.SwallowEventPublisher;
import com.dianping.puma.biz.monitor.event.PumaTaskStateEvent;
import com.dianping.puma.biz.service.PumaTaskStateService;
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
		event.setServerName(pumaServerConfig.getName());
		event.setTaskStates(pumaTaskStateService.findAll());
		pumaTaskStatePublisher.publish(event);
	}
}
