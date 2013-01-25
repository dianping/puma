package com.dianping.puma.syncserver.job.executor.builder;

import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.DumpTaskExecutor;

@Service("dumpTaskExecutorBuilder")
public class DumpTaskExecutorStrategy implements TaskExecutorStrategy<DumpTask, DumpTaskExecutor> {

    @Override
    public DumpTaskExecutor build(DumpTask task) {
        //根据Task创建TaskExecutor
        DumpTaskExecutor excutor = new DumpTaskExecutor(task);
        return excutor;
    }

    @Override
    public Type getType() {
        return Type.DUMP;
    }
}
