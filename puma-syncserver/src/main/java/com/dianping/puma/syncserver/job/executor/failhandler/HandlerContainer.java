package com.dianping.puma.syncserver.job.executor.failhandler;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

public class HandlerContainer implements InitializingBean {

    private static HandlerContainer instance;

    public static HandlerContainer getInstance() {
        return instance;
    }

    private Map<String, Handler> handlers;

    public Map<String, Handler> getHandlers() {
        return handlers;
    }

    public void setHandlers(Map<String, Handler> handlers) {
        this.handlers = handlers;
    }

    public Handler getHandler(String handlerName) {
        if (handlers != null) {
            return handlers.get(handlerName);
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

}
