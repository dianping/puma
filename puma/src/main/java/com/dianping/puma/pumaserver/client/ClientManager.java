package com.dianping.puma.pumaserver.client;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.common.model.ClientAck;
import com.dianping.puma.server.model.ClientConfig;
import com.dianping.puma.server.model.ClientConnect;
import com.dianping.puma.pumaserver.client.exception.PumaClientManageException;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientManager extends LifeCycle {

    void addClientAck(String clientName, ClientAck clientAck) throws PumaClientManageException;

    void addClientConfig(String clientName, ClientConfig clientConfig) throws PumaClientManageException;

    void addClientConnect(String clientName, ClientConnect clientConnect) throws PumaClientManageException;
}
