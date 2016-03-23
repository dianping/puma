package com.dianping.puma.common.service;

import com.dianping.puma.common.model.ClientAck;

/**
 * Created by xiaotian.li on 16/3/3.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientAckService {

    ClientAck find(String clientName);

    int update(String clientName, ClientAck clientAck);
}
