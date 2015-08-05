package com.dianping.puma.syncserver.manager.server;

import com.dianping.puma.core.util.IPUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ScheduledServerManager implements ServerManager {

	@Override
	public Collection<String> findAuthorizedHosts() {
		return IPUtils.getNoLoopbackIP4Addresses();
	}

	@Override
	public void register() {

	}

	@Scheduled(fixedDelay = 5 * 1000)
	private void scheduledRegister() {
		register();
	}
}
