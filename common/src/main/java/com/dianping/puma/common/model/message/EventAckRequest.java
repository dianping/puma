package com.dianping.puma.common.model.message;

import com.dianping.puma.common.model.ClientAck;

/**
 * Created by xiaotian.li on 16/3/12.
 * Email: lixiaotian07@gmail.com
 */
public class EventAckRequest extends EventRequest {

    private ClientAck clientAck;

    public ClientAck getClientAck() {
        return clientAck;
    }

    public void setClientAck(ClientAck clientAck) {
        this.clientAck = clientAck;
    }
}
