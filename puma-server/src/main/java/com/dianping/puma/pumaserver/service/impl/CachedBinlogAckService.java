package com.dianping.puma.pumaserver.service.impl;

import com.dianping.puma.core.netty.entity.BinlogAck;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.exception.BinlogAckException;

public class CachedBinlogAckService implements BinlogAckService {

	@Override
	public void save(String clientName, BinlogAck binlogAck) throws BinlogAckException {

	}

	@Override
	public BinlogAck load(String clientName) throws BinlogAckException {
		return null;
	}
}
