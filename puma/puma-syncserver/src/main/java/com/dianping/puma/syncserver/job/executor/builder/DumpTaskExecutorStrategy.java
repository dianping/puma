package com.dianping.puma.syncserver.job.executor.builder;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.DumpTaskExecutor;

@Service("dumpTaskExecutorStrategy")
public class DumpTaskExecutorStrategy implements TaskExecutorStrategy<DumpTask, DumpTaskExecutor> {

    @Override
    public DumpTaskExecutor build(DumpTask task) {
        //根据Task创建TaskExecutor
        DumpTaskExecutor excutor;
        try {
            excutor = new DumpTaskExecutor(task);
            return excutor;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Type getType() {
        return Type.DUMP;
    }
}
