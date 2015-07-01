package com.dianping.puma.pumaserver.ack.impl;

import com.dianping.puma.core.netty.entity.BinlogAck;
import com.dianping.puma.pumaserver.ack.BinlogAckService;

public class CachedBinlogAckService implements BinlogAckService {

	@Override
	public void save(String clientName, BinlogAck binlogAck) {

	}

	@Override
	public BinlogAck load(String clientName) {
		return null;
	}
}
