package com.dianping.puma.server.server;

import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.utils.IPUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DefaultTaskServerManager implements TaskServerManager {

	@Autowired
	PumaServerService pumaServerService;

	@Override
	public String findSelfHost() {
		return IPUtils.getFirstNoLoopbackIP4Address();
	}

	@Override
	public void register() {
		String host = findSelfHost();
		pumaServerService.registerByHost(host);
	}

	@Scheduled(fixedDelay = 5 * 1000)
	private void heartbeat() {
		register();
	}
}
