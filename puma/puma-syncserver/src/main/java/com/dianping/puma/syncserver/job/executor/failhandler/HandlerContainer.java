package com.dianping.puma.syncserver.job.executor.failhandler;

import java.util.Map;

public class HandlerContainer {

    private Map<String, Handler> handlers;

    public Map<String, Handler> getHandlers() {
        return handlers;
    }

    public void setHandlers(Map<String, Handler> handlers) {
        this.handlers = handlers;
    }

    public static Handler getHandler(String handlerName) {
        // TODO Auto-generated method stub
        return null;
    }

}
