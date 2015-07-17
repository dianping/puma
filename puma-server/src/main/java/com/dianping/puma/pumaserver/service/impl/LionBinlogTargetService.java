package com.dianping.puma.pumaserver.service.impl;

import com.dianping.puma.core.dto.BinlogTarget;
import com.dianping.puma.pumaserver.exception.binlog.BinlogTargetException;
import com.dianping.puma.pumaserver.service.BinlogTargetService;

public class LionBinlogTargetService implements BinlogTargetService {

    @Override
    public BinlogTarget find(String clientName) throws BinlogTargetException {
        BinlogTarget t = new BinlogTarget();
        t.setTargetName("dozer");
        return t;
    }
}
