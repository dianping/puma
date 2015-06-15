package com.dianping.puma.admin.api.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.puma.admin.cache.StateCacheService;
import com.dianping.puma.admin.common.StateContainer;
import com.dianping.puma.core.api.StateAckService;
import com.dianping.puma.core.model.ClientAck;
import com.dianping.puma.core.model.ServerAck;

@Service
@Component
public class StateAckServiceImpl implements StateAckService {
	private static final Logger LOG = LoggerFactory.getLogger(StateAckServiceImpl.class);

	@Autowired
	private StateContainer stateContainer;
	
	@Autowired
	private StateCacheService stateCacheService;

	@Override
	public void setClientAck(ClientAck clientAck) {
		stateContainer.setClientAckInfo(clientAck);
		LOG.info("set Client ack info.");
	}
	
	@Override
	public ClientAck getClientAck(String clientName) {
		ClientAck clientAck = stateCacheService.popClientAck(clientName);
		LOG.info("get Client ack info.");
		return clientAck;
	}

	@Override
	public void setServerAck(ServerAck serverAck) {
		stateContainer.setServerAckInfo(serverAck);
		LOG.info("set Server ack info.");
	}

	@Override
	public void setServerAcks(List<ServerAck> serverAcks) {
		LOG.info("set Server ack infos.");
		if (serverAcks == null || serverAcks.size() == 0) {
			return;
		}
		for (ServerAck serverAck : serverAcks) {
			stateContainer.setServerAckInfo(serverAck);
		}
	}
}
