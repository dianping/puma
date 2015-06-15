package com.dianping.puma.api.service.impl;

import com.dianping.puma.api.service.PositionService;
import com.dianping.puma.core.model.BinlogInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service("positionService")
public class PigeonPositionService implements PositionService {

	@Override
	public Pair<BinlogInfo, Long> request() {
		return null;
	}

	@Override
	public void ack(Pair<BinlogInfo, Long> pair) {

	}
}
