package com.dianping.puma.core.dto.binlog.request;

import com.dianping.puma.core.dto.BinlogRollback;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class BinlogRollbackRequest extends BinlogRequest {

    private BinlogRollback binlogRollback;

    public BinlogRollback getBinlogRollback() {
        return binlogRollback;
    }

    public void setBinlogRollback(BinlogRollback binlogRollback) {
        this.binlogRollback = binlogRollback;
    }
}
