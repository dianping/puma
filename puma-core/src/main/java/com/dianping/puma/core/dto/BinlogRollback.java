package com.dianping.puma.core.dto;

import com.dianping.puma.core.model.BinlogInfo;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class BinlogRollback {
    private BinlogInfo binlogInfo;

    public BinlogInfo getBinlogInfo() {
        return binlogInfo;
    }

    public void setBinlogInfo(BinlogInfo binlogInfo) {
        this.binlogInfo = binlogInfo;
    }
}
