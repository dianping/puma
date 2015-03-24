package com.dianping.puma.monitor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ClientStatus;
import com.dianping.puma.config.TaskLionConfig;
import com.dianping.puma.core.entity.PumaClientInfoEntity;
import com.dianping.puma.service.PumaClientInfoService;

@Component("pumaMonitorSeqTask")
public class PumaMonitorSeqTask implements PumaMonitorTask {

	@Autowired
	private PumaClientInfoService pumaClientInfoService;

	@Autowired
	private TaskLionConfig intervalConfig;

	/*
	 * public void runTask() { Map<String, ClientStatus> clientStatuses =
	 * SystemStatusContainer.instance.listClientStatus(); Map<String, Long>
	 * clientSuccessSeq = SystemStatusContainer.instance.listClientSuccessSeq();
	 * for(Map.Entry<String,ClientStatus>
	 * clientStatus:clientStatuses.entrySet()){ PumaClientInfoEntity entity=new
	 * PumaClientInfoEntity(); entity.setName(clientStatus.getKey());
	 * entity.setIp(clientStatus.getValue().getIp());
	 * entity.setTarget(clientStatus.getValue().getTarget());
	 * if(clientSuccessSeq.containsKey(clientStatus.getKey())){
	 * entity.setSeq(clientSuccessSeq.get(clientStatus.getKey()).longValue());
	 * pumaClientInfoService.create(entity); } } }
	 */

	public void runTask() {
		Map<String, ClientStatus> clientStatuses = SystemStatusContainer.instance.listClientStatus();
		Map<String, Long> clientSuccessSeq = SystemStatusContainer.instance.listClientSuccessSeq();
		for (Map.Entry<String, ClientStatus> clientStatus : clientStatuses.entrySet()) {
			if (clientSuccessSeq.containsKey(clientStatus.getKey())) {
				Cat.getProducer().logEvent(
						"Puma.server." + clientStatus.getKey() + ".seq",
						"sequence",
						Message.SUCCESS,
						"name = " + clientStatus.getKey() + "&target = " + clientStatus.getValue().getTarget()
								+ "&seq=" + clientSuccessSeq.get(clientStatus.getKey()).longValue() + "&duration = "
								+ intervalConfig.getServerInfoInterval());
			}
		}
	}

}
