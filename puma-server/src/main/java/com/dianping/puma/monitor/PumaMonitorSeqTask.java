package com.dianping.puma.monitor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ClientStatus;
import com.dianping.puma.core.entity.PumaClientInfoEntity;
import com.dianping.puma.service.PumaClientInfoService;

@Component("pumaMonitorSeqTask")
public class PumaMonitorSeqTask implements PumaMonitorTask {
	
	@Autowired
	private PumaClientInfoService pumaClientInfoService;
	
	public void runTask() {
		Map<String, ClientStatus> clientStatuses = SystemStatusContainer.instance.listClientStatus();
		Map<String, Long> clientSuccessSeq =  SystemStatusContainer.instance.listClientSuccessSeq();
		for(Map.Entry<String,ClientStatus> clientStatus:clientStatuses.entrySet()){
			PumaClientInfoEntity entity=new PumaClientInfoEntity();
			entity.setName(clientStatus.getKey());
			entity.setIp(clientStatus.getValue().getIp());
			entity.setTarget(clientStatus.getValue().getTarget());
			entity.setSeq(clientSuccessSeq.get(clientStatus.getKey()).longValue());
			pumaClientInfoService.create(entity);
		}
	}
}
