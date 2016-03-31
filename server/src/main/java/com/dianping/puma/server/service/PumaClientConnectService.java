package com.dianping.puma.server.service;

import com.dianping.puma.server.model.ClientConnect;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientConnectService {

    int update(String clientName, ClientConnect clientConnect);
}
