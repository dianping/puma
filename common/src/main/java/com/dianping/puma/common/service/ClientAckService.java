package com.dianping.puma.common.service;

import com.dianping.puma.common.model.ClientAck;

/**
 * Created by xiaotian.li on 16/3/3.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAckService {

    ClientAck find(String clientName);

    void create(String clientName, ClientAck clientAck);

    int modify(String clientName, ClientAck clientAck);

    void replace(String clientName, ClientAck clientAck);

    int remove(String clientName);
}
