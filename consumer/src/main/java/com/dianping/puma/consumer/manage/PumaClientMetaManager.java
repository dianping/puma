package com.dianping.puma.consumer.manage;

import com.dianping.puma.common.PumaLifeCycle;
import com.dianping.puma.consumer.exception.PumaClientMetaManageException;
import com.dianping.puma.consumer.ha.PumaClientCleanable;
import com.dianping.puma.consumer.model.ClientConfig;
import com.dianping.puma.consumer.model.ClientConnect;
import com.dianping.puma.consumer.model.ClientToken;

/**
 * Created by xiaotian.li on 16/3/30.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientMetaManager extends PumaClientCleanable, PumaLifeCycle {

    ClientToken findClientToken(String clientName) throws PumaClientMetaManageException;

    ClientConfig findClientConfig(String clientName) throws PumaClientMetaManageException;

    ClientConnect findClientConnect(String clientName) throws PumaClientMetaManageException;

    void registerClientToken(String clientName, ClientToken clientToken) throws PumaClientMetaManageException;

    void registerClientConfig(String clientName, ClientConfig clientConfig) throws PumaClientMetaManageException;

    void registerClientConnect(String clientName, ClientConnect clientConnect) throws PumaClientMetaManageException;
}
