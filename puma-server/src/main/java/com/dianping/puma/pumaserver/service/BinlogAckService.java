package com.dianping.puma.pumaserver.service;

import com.dianping.puma.core.netty.entity.BinlogAck;
import com.dianping.puma.pumaserver.exception.BinlogAckException;

public interface BinlogAckService {

	void save(String clientName, BinlogAck binlogAck) throws BinlogAckException;

	BinlogAck load(String clientName) throws BinlogAckException;
}
