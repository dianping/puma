package com.dianping.puma.server.manage;

import com.dianping.puma.common.PumaLifeCycle;
import com.dianping.puma.common.model.ClientConfig;
import com.dianping.puma.server.exception.PumaClientMetaManageException;

/**
 * Created by xiaotian.li on 16/3/30.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientMetaManager extends PumaLifeCycle {

    void addClientConfig(String clientName, ClientConfig clientConfig) throws PumaClientMetaManageException;
}
