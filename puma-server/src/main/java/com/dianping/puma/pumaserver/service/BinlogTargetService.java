package com.dianping.puma.pumaserver.service;

import com.dianping.puma.core.netty.entity.BinlogTarget;
import com.dianping.puma.pumaserver.service.exception.BinlogTargetException;

public interface BinlogTargetService {

	BinlogTarget find(String clientName) throws BinlogTargetException;
}
