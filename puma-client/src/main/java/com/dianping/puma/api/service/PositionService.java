package com.dianping.puma.api.service;

import com.dianping.puma.core.model.BinlogInfo;
import org.apache.commons.lang3.tuple.Pair;

public interface PositionService {

	Pair<BinlogInfo, Long> request();

	void ack(Pair<BinlogInfo, Long> pair);
}
