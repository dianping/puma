package com.dianping.puma.pumaserver.client;

import com.dianping.puma.biz.model.ClientConfig;
import com.dianping.puma.biz.model.ClientConnect;
import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.pumaserver.client.exception.PumaClientManageException;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientManager extends LifeCycle {

    void putConfig(String clientName, ClientConfig clientConfig) throws PumaClientManageException;

    void putConnect(String clientName, ClientConnect clientConnect) throws PumaClientManageException;

    void remove(String clientName) throws PumaClientManageException;
}
