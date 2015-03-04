package com.dianping.puma.monitor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ClientStatus;
import com.dianping.puma.config.TaskIntervalConfig;

@Component("pumaMonitorClientIpTask")
public class PumaMonitorClientIpTask implements PumaMonitorTask {

	@Autowired
	private TaskIntervalConfig intervalConfig;
	
	@Override
	public void runTask() {
		Map<String, ClientStatus> clientStatuses = SystemStatusContainer.instance.listClientStatus();
		for(Map.Entry<String,ClientStatus> clientStatus:clientStatuses.entrySet()){
			Cat.getProducer().logEvent("Puma.server."+clientStatus.getKey()+".ip", clientStatus.getValue().getIp(), Message.SUCCESS,"name = "+clientStatus.getKey()+"&duration = "+intervalConfig.getClientIpInterval());
		}
	}

}
