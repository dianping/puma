package com.dianping.puma.pumaserver.ack.impl;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.entity.BinlogAck;
import com.dianping.puma.pumaserver.ack.BinlogAckService;

public class CachedBinlogAckService implements BinlogAckService {

	@Override
	public void save(String clientName, BinlogInfo binlogInfo) {

	}

	@Override
	public BinlogAck load(String clientName) {
		return null;
	}
}
