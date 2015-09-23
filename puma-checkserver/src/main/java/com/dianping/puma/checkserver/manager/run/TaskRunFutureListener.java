package com.dianping.puma.checkserver.manager.run;

import com.dianping.puma.checkserver.model.TaskResult;
import com.google.common.util.concurrent.FutureCallback;

public interface TaskRunFutureListener extends FutureCallback<TaskResult> {

    @Override
    void onSuccess(TaskResult result);

    @Override
    void onFailure(Throwable cause);

}
