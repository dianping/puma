package com.dianping.puma.pumaserver.service;

import com.dianping.puma.core.netty.entity.binlog.request.BinlogAckRequest;
import com.dianping.puma.pumaserver.exception.BinlogAckException;

public interface BinlogAckService {

	void save(String clientName, BinlogAckRequest binlogAckRequest) throws BinlogAckException;

	BinlogAckRequest load(String clientName) throws BinlogAckException;
}
