package com.dianping.puma.checkserver.manager.run;

import com.dianping.puma.checkserver.model.TaskResult;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class TaskRunFuture extends FutureTask<TaskResult> {

    private final TaskRunFutureListener listener;

    public TaskRunFuture(Callable<TaskResult> callable, TaskRunFutureListener listener) {
        super(callable);
        this.listener = listener;
    }

    @Override
    protected void done() {
        super.done();

        if (listener == null) {
            return;
        }

        try {
            listener.onSuccess(get());
        } catch (ExecutionException e) {
            listener.onFailure(e.getCause());
            return;
        } catch (InterruptedException e) {
            return;
        }
    }
}
