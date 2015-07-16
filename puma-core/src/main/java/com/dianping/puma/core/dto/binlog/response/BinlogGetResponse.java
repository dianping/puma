package com.dianping.puma.core.dto.binlog.response;

import com.dianping.puma.core.dto.BinlogMessage;

public class BinlogGetResponse {

    private BinlogMessage binlogMessage;

    public BinlogGetResponse setBinlogMessage(BinlogMessage binlogMessage) {
        this.binlogMessage = binlogMessage;
        return this;
    }

    public BinlogMessage getBinlogMessage() {
        return binlogMessage;
    }
}
