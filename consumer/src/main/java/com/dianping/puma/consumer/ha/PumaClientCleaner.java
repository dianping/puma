package com.dianping.puma.consumer.ha;

import com.dianping.puma.common.PumaLifeCycle;
import com.dianping.puma.consumer.exception.PumaClientCleanException;
import com.dianping.puma.consumer.model.ClientToken;

/**
 * Created by xiaotian.li on 16/4/1.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientCleaner extends PumaLifeCycle {

    void registerClientToken(String clientName, ClientToken clientToken) throws PumaClientCleanException;

    void clean(String clientName) throws PumaClientCleanException;
}
