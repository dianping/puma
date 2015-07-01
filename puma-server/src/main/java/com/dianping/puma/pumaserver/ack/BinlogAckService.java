package com.dianping.puma.pumaserver.ack;

import com.dianping.puma.core.netty.entity.BinlogAck;

public interface BinlogAckService {

	void save(String clientName, BinlogAck binlogAck);

	BinlogAck load(String clientName);
}
