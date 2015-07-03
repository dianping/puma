package com.dianping.puma.core.netty.entity.binlog.request;

import com.dianping.puma.core.netty.entity.BinlogAck;

public class BinlogAckRequest extends BinlogRequest {

    private BinlogAck binlogAck;

    public BinlogAck getBinlogAck() {
        return binlogAck;
    }

    public void setBinlogAck(BinlogAck binlogAck) {
        this.binlogAck = binlogAck;
    }
}
