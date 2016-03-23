package com.dianping.puma.common.service;

import com.dianping.puma.common.model.ClientConnect;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientConnectService {

    int update(String clientName, ClientConnect clientConnect);
}
