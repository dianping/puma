package com.dianping.puma.monitor;

import com.dianping.cat.Cat;
import com.dianping.puma.core.exception.MonitorException;
import com.dianping.puma.core.model.container.client.ClientStateContainer;
import com.dianping.puma.core.model.state.client.ClientState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("clientConnectMonitor")
public class ClientConnectMonitor {

	private static final Logger LOG = LoggerFactory.getLogger(ClientConnectMonitor.class);

	@Autowired
	ClientStateContainer clientStateContainer;

	@PostConstruct
	public void init() {

	}

	@Scheduled(cron = "0/60 * * * * ?")
	public void clientConnectedMonitor() throws MonitorException {
		try {
			for (ClientState clientState: clientStateContainer.getAll()) {
				Cat.logEvent("ClientConnect.connected", clientState.getName());
			}
		} catch (Exception e) {
			LOG.warn("Monitor client connect error: {}.", e.getStackTrace());
			throw new MonitorException(e);
		}
	}
}
