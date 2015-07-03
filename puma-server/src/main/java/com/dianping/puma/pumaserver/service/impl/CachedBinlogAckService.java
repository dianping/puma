package com.dianping.puma.pumaserver.service.impl;

import com.dianping.puma.core.netty.entity.binlog.request.BinlogAckRequest;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.exception.binlog.BinlogAckException;

public class CachedBinlogAckService implements BinlogAckService {

	@Override
	public void save(String clientName, BinlogAckRequest binlogAckRequest) throws BinlogAckException {

	}

	@Override
	public BinlogAckRequest load(String clientName) throws BinlogAckException {
		return null;
	}
}
