package com.dianping.puma.server.server;

import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.core.util.IPUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class DefaultTaskServerManager implements TaskServerManager {

	@Autowired
	PumaServerService pumaServerService;

	@Override
	public Collection<String> findAuthorizedHosts() {
		return IPUtils.getNoLoopbackIP4Addresses();
	}

	@Override
	public void register() {
		for (String host: findAuthorizedHosts()) {
			pumaServerService.registerByHost(host);
		}
	}

	@Scheduled(fixedDelay = 5 * 1000)
	private void heartbeat() {
		register();
	}
}
