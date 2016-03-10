package com.dianping.puma.core.dto.binlog.response;

import com.dianping.puma.core.dto.BinlogHttpMessage;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
public abstract class BinlogResponse extends BinlogHttpMessage {

    private String clientName;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
