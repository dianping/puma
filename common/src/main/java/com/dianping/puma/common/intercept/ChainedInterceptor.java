package com.dianping.puma.common.intercept;

import com.dianping.puma.common.intercept.exception.PumaInterceptException;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
public class ChainedInterceptor<T> extends AbstractPumaInterceptor<T> {

    private List<PumaInterceptor<T>> interceptors;

    @Override
    public void start() {
        super.start();

        for (PumaInterceptor<T> interceptor : interceptors) {
            interceptor.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (PumaInterceptor<T> interceptor : interceptors) {
            interceptor.stop();
        }
    }

    @Override
    public void before(T data) throws PumaInterceptException {
        for (PumaInterceptor<T> interceptor : interceptors) {
            interceptor.before(data);
        }
    }

    @Override
    public void after(T data) throws PumaInterceptException {
        for (PumaInterceptor<T> interceptor : interceptors) {
            interceptor.after(data);
        }
    }

    @Override
    public void error(T data) throws PumaInterceptException {
        for (PumaInterceptor<T> interceptor : interceptors) {
            interceptor.error(data);
        }
    }

    public void setInterceptors(List<PumaInterceptor<T>> interceptors) {
        this.interceptors = interceptors;
    }
}
