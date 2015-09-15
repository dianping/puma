package com.dianping.puma.checkserver.manager.server;

import com.dianping.puma.core.util.IPUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class DefaultCheckTaskServerManager implements CheckTaskServerManager {

	@Override
	public String findFirstAuthorizedHost() {
		return IPUtils.getFirstNoLoopbackIP4Address();
	}

	@Override
	public Collection<String> findAuthorizedHosts() {
		return IPUtils.getNoLoopbackIP4Addresses();
	}
}
