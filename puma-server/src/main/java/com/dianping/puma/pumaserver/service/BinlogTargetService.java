package com.dianping.puma.pumaserver.service;

import com.dianping.puma.core.dto.BinlogTarget;
import com.dianping.puma.pumaserver.exception.binlog.BinlogTargetException;

public interface BinlogTargetService {

	BinlogTarget find(String targetKey) throws BinlogTargetException;
}
