package com.dianping.puma.admin.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.puma.core.api.StateReporterService;
import com.dianping.puma.core.model.RelatedInfo;
import com.dianping.puma.core.model.ClientAck;
import com.dianping.puma.core.model.ServerAck;

@Service
public class StateReporterServiceImpl implements StateReporterService {
	private static final Logger LOG = LoggerFactory.getLogger(StateReporterServiceImpl.class);

	@Override
	public RelatedInfo getRelatedInfo(String clientName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClientAck(ClientAck clientAck) {
		// TODO Auto-generated method stub
		LOG.info("Client ack info.");
	}

	@Override
	public void setServerAck(ServerAck serverAck) {
		// TODO Auto-generated method stub

	}

}
