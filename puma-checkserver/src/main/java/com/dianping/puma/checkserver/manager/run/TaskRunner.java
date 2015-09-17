package com.dianping.puma.checkserver.manager.run;

import com.dianping.puma.biz.entity.CheckTaskEntity;

public interface TaskRunner {

    TaskRunFuture run(CheckTaskEntity checkTask, TaskRunFutureListener listener);

    boolean isFull();
}
