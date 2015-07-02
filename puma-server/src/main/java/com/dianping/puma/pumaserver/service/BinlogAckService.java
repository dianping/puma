package com.dianping.puma.pumaserver.service;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.entity.BinlogAck;

public interface BinlogAckService {

	void save(String clientName, BinlogInfo binlogInfo);

	BinlogAck load(String clientName);
}
