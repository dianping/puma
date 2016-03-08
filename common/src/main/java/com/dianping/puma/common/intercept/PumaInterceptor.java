package com.dianping.puma.common.intercept;

import com.dianping.puma.common.PumaLifeCycle;
import com.dianping.puma.common.intercept.exception.PumaInterceptException;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaInterceptor<T> extends PumaLifeCycle {

    void before(T data) throws PumaInterceptException;

    void after(T data) throws PumaInterceptException;

    void error(T data) throws PumaInterceptException;
}
