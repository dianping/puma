package com.dianping.puma.pumaserver.service;

import com.dianping.puma.core.netty.entity.BinlogTarget;
import com.dianping.puma.pumaserver.exception.BinlogTargetException;

public interface BinlogTargetService {

	BinlogTarget find(String clientName) throws BinlogTargetException;
}
