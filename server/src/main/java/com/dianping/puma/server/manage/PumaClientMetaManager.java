package com.dianping.puma.server.manage;

import com.dianping.puma.common.PumaLifeCycle;
import com.dianping.puma.server.model.ClientConfig;
import com.dianping.puma.server.model.ClientConnect;
import com.dianping.puma.server.exception.PumaClientMetaManageException;
import com.dianping.puma.server.model.ClientToken;

/**
 * Created by xiaotian.li on 16/3/30.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientMetaManager extends PumaLifeCycle {

    ClientToken findClientToken(String clientName) throws PumaClientMetaManageException;

    ClientConfig findClientConfig(String clientName) throws PumaClientMetaManageException;

    ClientConnect findClientConnect(String clientName) throws PumaClientMetaManageException;

    void registerClientToken(String clientName, ClientToken clientToken) throws PumaClientMetaManageException;

    void registerClientConfig(String clientName, ClientConfig clientConfig) throws PumaClientMetaManageException;

    void registerClientConnect(String clientName, ClientConnect clientConnect) throws PumaClientMetaManageException;

    void lost(String clientName) throws PumaClientMetaManageException;
}
