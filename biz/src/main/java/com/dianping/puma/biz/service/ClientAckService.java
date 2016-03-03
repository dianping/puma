package com.dianping.puma.biz.service;

import com.dianping.puma.biz.model.ClientAck;

/**
 * Created by xiaotian.li on 16/3/3.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAckService {

    void create(String clientName, ClientAck clientAck);

    int modify(String clientName, ClientAck clientAck);

    void replace(String clientName, ClientAck clientAck);

    int remove(String clientName);
}
