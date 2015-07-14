package com.dianping.puma.server.server;

import com.dianping.puma.core.util.IPUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class DefaultTaskServerManager implements TaskServerManager {

	@Override
	public Collection<String> findAuthorizedHosts() {
		return IPUtils.getNoLoopbackIP4Addresses();
	}
}
