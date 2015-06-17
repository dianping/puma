package com.dianping.puma.admin.api.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
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
		if (clientAck != null && StringUtils.isNotBlank(clientAck.getClientName())) {
			stateContainer.setClientAckInfo(clientAck);
			LOG.info("ClientName: {} set Client ack info.", clientAck.getClientName());
		} else {
			LOG.info("Not valid set Client ack info.");
		}
	}

	@Override
	public ClientAck getClientAck(String clientName) {
		ClientAck clientAck = null;
		if (StringUtils.isNotBlank(clientName)) {
			clientAck = stateCacheService.popClientAck(clientName);
			LOG.info("ClientName: {} get Client ack info.", clientAck.getClientName());
		} else {
			LOG.info("Not valid get Client ack info.");
		}
		return clientAck;
	}

	@Override
	public void setServerAck(ServerAck serverAck) {
		if (serverAck != null && StringUtils.isNotBlank(serverAck.getClientName())) {
			stateContainer.setServerAckInfo(serverAck);
			LOG.info("ServerName: {} set Server ack info.",
					StringUtils.isNotBlank(serverAck.getServerName()) ? serverAck.getServerName() : "");
		} else {
			LOG.info("Not valid set Server ack info.");
		}
	}

	@Override
	public void setServerAcks(List<ServerAck> serverAcks) {
		LOG.info("set Server acks info.");
		if (serverAcks == null || serverAcks.size() == 0) {
			return;
		}
		for (ServerAck serverAck : serverAcks) {
			if (serverAck != null && StringUtils.isNotBlank(serverAck.getClientName())) {
				stateContainer.setServerAckInfo(serverAck);
			}else{
				LOG.info("Not valid set Server ack info.");
			}
		}
	}
}
