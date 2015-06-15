package com.dianping.puma.api.service.impl;

import com.dianping.pigeon.remoting.invoker.config.annotation.Reference;
import com.dianping.puma.api.service.PositionService;
import com.dianping.puma.core.api.StateAckService;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.ClientAck;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("positionService")
public class PigeonPositionService implements PositionService {

	@Reference(timeout = 1000)
	private StateAckService stateAckService;

	@Override
	public Pair<BinlogInfo, Long> request(String clientName) {
		ClientAck clientAck = stateAckService.getClientAck(clientName);
		return Pair.of(clientAck.getBinlogInfo(), clientAck.getCreateDate().getTime());
	}

	@Override
	public void ack(String clientName, Pair<BinlogInfo, Long> pair) {
		ClientAck clientAck = new ClientAck();
		clientAck.setBinlogInfo(pair.getLeft());
		clientAck.setCreateDate(new Date(pair.getRight()));
		stateAckService.setClientAck(clientAck);
	}
}
