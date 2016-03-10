package com.dianping.puma.biz.service;

import com.dianping.puma.common.model.ClientConnect;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientConnectService {

    void create(String clientName, ClientConnect clientConnect);

    int modify(String clientName, ClientConnect clientConnect);

    void replace(String clientName, ClientConnect clientConnect);

    int remove(String clientName);
}
