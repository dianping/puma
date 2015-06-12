package com.dianping.puma.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dianping.pigeon.remoting.invoker.config.annotation.Reference;
import com.dianping.puma.common.SystemStatusContainer.ClientStatus;
import com.dianping.puma.common.SystemStatusContainer.ServerStatus;
import com.dianping.puma.core.api.StateAckService;
import com.dianping.puma.core.model.ServerAck;

@Component("serverAckService")
public class ServerAckService {
	
	@Reference(timeout = 1000)
	private StateAckService stateReporterService;
	
	public void pushServerAcks() {
		List<ServerAck> serverAcks =null;
		stateReporterService.setServerAcks(serverAcks);
	}
	
	private List<ServerAck> collectServerAck(){
		List<ServerAck> serverAcks=new ArrayList<ServerAck>();
		Map<String,ClientStatus> clientStatuses = SystemStatusContainer.instance.listClientStatus();
		Map<String,ServerStatus> serverStatuses = SystemStatusContainer.instance.listServerStatus();
		for(Map.Entry<String, ClientStatus> clientStatus: clientStatuses){
			
		}
		return serverAcks;
	}
}
