package com.dianping.puma.core.dto.binlog.response;

import com.dianping.puma.core.dto.BinlogMessage;

public class BinlogGetResponse {

    private BinlogMessage binlogMessage;

    public void setBinlogMessage(BinlogMessage binlogMessage) {
        this.binlogMessage = binlogMessage;
    }

    public BinlogMessage getBinlogMessage() {
        return binlogMessage;
    }
}
