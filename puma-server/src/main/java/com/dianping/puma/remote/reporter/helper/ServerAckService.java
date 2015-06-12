package com.dianping.puma.remote.reporter.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.pigeon.remoting.invoker.config.annotation.Reference;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ClientStatus;
import com.dianping.puma.common.SystemStatusContainer.ServerStatus;
import com.dianping.puma.config.PumaServerConfig;
import com.dianping.puma.core.api.StateAckService;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.ServerAck;

@Component("serverAckService")
public class ServerAckService {

	private static final Logger LOG = LoggerFactory.getLogger(ServerAckService.class);

	@Reference(timeout = 1000)
	private StateAckService stateReporterService;
	@Autowired
	private PumaServerConfig pumaServerConfig;

	public void pushServerAcks() {
		List<ServerAck> serverAcks = collectServerAck();
		stateReporterService.setServerAcks(serverAcks);
		LOG.info("Server send acks.");
	}

	private List<ServerAck> collectServerAck() {
		List<ServerAck> serverAcks = new ArrayList<ServerAck>();
		Map<String, ClientStatus> clientStatuses = SystemStatusContainer.instance.listClientStatus();
		Map<String, ServerStatus> serverStatuses = SystemStatusContainer.instance.listServerStatus();
		for (Map.Entry<String, ClientStatus> clientStatus : clientStatuses.entrySet()) {
			ServerAck serverAck = new ServerAck();
			serverAck.setClientName(clientStatus.getKey());
			serverAck.setClientIp(clientStatus.getValue().getIp());
			ServerStatus serverStatus = serverStatuses.get(clientStatus.getValue().getTarget());
			serverAck.setParserBinlog(new BinlogInfo(serverStatus.getBinlogFile(), serverStatus.getBinlogPos()));
			serverAck.setSenderBinlog(clientStatus.getValue().getBinlogInfo());
			serverAck.setTaskName(clientStatus.getValue().getTarget());
			serverAck.setServerIp(pumaServerConfig.getHost());
			serverAck.setServerName(pumaServerConfig.getName());
			serverAck.setCreateDate(new Date());
		}
		return serverAcks;
	}
}
