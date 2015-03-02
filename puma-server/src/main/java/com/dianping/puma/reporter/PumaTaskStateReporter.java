package com.dianping.puma.reporter;

import com.dianping.puma.config.Config;
import com.dianping.puma.core.container.PumaTaskStateContainer;
import com.dianping.puma.core.monitor.PumaTaskStateEvent;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PumaTaskStateReporter {

	private static final Logger LOG = LoggerFactory.getLogger(PumaTaskStateReporter.class);

	@Autowired
	SwallowEventPublisher pumaTaskStatePublisher;

	@Autowired
	Config pumaServerConfig;

	@Autowired
	PumaTaskStateContainer pumaTaskStateContainer;

	@Scheduled(cron = "0/5 * * * * ?")
	public void report() {
		try {
			PumaTaskStateEvent event = new PumaTaskStateEvent();
			event.setPumaServerName(pumaServerConfig.getName());
			event.setTaskIds(pumaTaskStateContainer.getAllTaskIds());
			event.setStates(pumaTaskStateContainer.getAll());
			pumaTaskStatePublisher.publish(event);
		} catch (Exception e) {
			LOG.error("Report puma task state error: {}.", e.getMessage());
		}
	}
}
