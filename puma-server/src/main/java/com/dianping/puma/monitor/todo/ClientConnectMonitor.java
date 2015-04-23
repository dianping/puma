package com.dianping.puma.monitor.todo;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.puma.core.exception.MonitorException;
import com.dianping.puma.core.model.container.client.ClientStateContainer;
import com.dianping.puma.core.model.state.client.ClientState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

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
			List<ClientState> clientStates = clientStateContainer.getAll();
			if (clientStates.size() == 0) {
				Cat.logEvent("ClientConnect.connected", "NO CLIENT", Event.SUCCESS, "");
			} else {
				for (ClientState clientState: clientStates) {
					Cat.logEvent("ClientConnect.connected", clientState.getName(), Event.SUCCESS, "");
				}
			}
		} catch (Exception e) {
			LOG.warn("Monitor client connect error: {}.", e.getStackTrace());
			throw new MonitorException(e);
		}
	}
}
