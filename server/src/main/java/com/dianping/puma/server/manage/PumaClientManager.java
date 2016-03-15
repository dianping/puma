package com.dianping.puma.server.manage;

import com.dianping.puma.common.PumaLifeCycle;
import com.dianping.puma.common.model.ClientConfig;
import com.dianping.puma.server.exception.PumaClientManageException;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientManager extends PumaLifeCycle {

    String subscribe(String clientName, ClientConfig clientConfig) throws PumaClientManageException;

    void unsubscribe(String clientName) throws PumaClientManageException;
}
