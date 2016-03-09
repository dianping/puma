package com.dianping.puma.core.dto.binlog.response;

import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.core.dto.BinlogMessage;

public class BinlogGetResponse extends BinlogResponse {

    private BinlogMessage binlogMessage;

    private transient BinlogGetRequest binlogGetRequest;

    public BinlogGetResponse setBinlogMessage(BinlogMessage binlogMessage) {
        this.binlogMessage = binlogMessage;
        return this;
    }

    public BinlogMessage getBinlogMessage() {
        return binlogMessage;
    }

    public BinlogGetRequest getBinlogGetRequest() {
        return binlogGetRequest;
    }

    public void setBinlogGetRequest(BinlogGetRequest binlogGetRequest) {
        this.binlogGetRequest = binlogGetRequest;
    }
}
