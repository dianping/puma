package com.dianping.puma.consumer.intercept;

import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.intercept.exception.PumaInterceptException;
import com.dianping.puma.core.dto.BinlogHttpMessage;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/22.
 * Email: lixiaotian07@gmail.com
 */
public class ChainedMessageInterceptor extends AbstractPumaLifeCycle implements PumaMessageInterceptor {

    private List<PumaConsumerInterceptor<BinlogHttpMessage>> interceptors;

    @Override
    public void start() {
        super.start();

        for (PumaConsumerInterceptor<BinlogHttpMessage> interceptor: interceptors) {
            interceptor.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (PumaConsumerInterceptor<BinlogHttpMessage> interceptor: interceptors) {
            interceptor.stop();
        }
    }

    @Override
    public void clean(String clientName) {
        for (PumaConsumerInterceptor<BinlogHttpMessage> interceptor: interceptors) {
            interceptor.clean(clientName);
        }
    }

    @Override
    public void before(BinlogHttpMessage data) throws PumaInterceptException {
        for (PumaConsumerInterceptor<BinlogHttpMessage> interceptor: interceptors) {
            interceptor.before(data);
        }
    }

    @Override
    public void after(BinlogHttpMessage data) throws PumaInterceptException {
        for (PumaConsumerInterceptor<BinlogHttpMessage> interceptor: interceptors) {
            interceptor.after(data);
        }
    }

    @Override
    public void error(BinlogHttpMessage data) throws PumaInterceptException {
        for (PumaConsumerInterceptor<BinlogHttpMessage> interceptor: interceptors) {
            interceptor.error(data);
        }
    }

    public void setInterceptors(List<PumaConsumerInterceptor<BinlogHttpMessage>> interceptors) {
        this.interceptors = interceptors;
    }
}
