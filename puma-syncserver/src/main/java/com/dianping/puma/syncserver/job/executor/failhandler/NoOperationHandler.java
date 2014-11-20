package com.dianping.puma.syncserver.job.executor.failhandler;

public class NoOperationHandler implements Handler {

    @Override
    public String getName() {
        return "NoOperation";
    }

    @Override
    public HandleResult handle(HandleContext context) {
        HandleResult result = new HandleResult();
        result.setIgnoreFailEvent(true);
        return result;
    }

}
