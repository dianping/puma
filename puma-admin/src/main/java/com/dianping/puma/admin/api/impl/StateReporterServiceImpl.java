package com.dianping.puma.admin.api.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.puma.admin.api.common.StateCacheService;
import com.dianping.puma.admin.api.common.StateContainer;
import com.dianping.puma.core.api.StateReporterService;
import com.dianping.puma.core.model.ClientRelatedInfo;
import com.dianping.puma.core.model.ClientAck;
import com.dianping.puma.core.model.ServerAck;

@Service
@Component
public class StateReporterServiceImpl implements StateReporterService {
	private static final Logger LOG = LoggerFactory.getLogger(StateReporterServiceImpl.class);

	@Autowired
	private StateContainer stateContainer;
	@Autowired
	private StateCacheService dCacheService;
	
	@Override
	public ClientRelatedInfo getRelatedInfo(String clientName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClientAck(ClientAck clientAck) {
		stateContainer.setClientAckInfo(clientAck);
		LOG.info("Client ack info.");
	}

	@Override
	public void setServerAck(ServerAck serverAck) {
		stateContainer.setServerAckInfo(serverAck);
		LOG.info("Server ack info.");
	}
	
	@Override
	public Map<String,ClientRelatedInfo> getRelatedInfos(List<String> clientNames){
		// TODO Auto-generated method stub
		return null;
	}

}
