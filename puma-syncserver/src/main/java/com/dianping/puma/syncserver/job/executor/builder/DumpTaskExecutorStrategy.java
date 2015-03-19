package com.dianping.puma.syncserver.job.executor.builder;

import java.io.IOException;

import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.DumpTask;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.service.DstDBInstanceService;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.DumpTaskExecutor;

@Service("dumpTaskExecutorStrategy")
public class DumpTaskExecutorStrategy implements TaskExecutorStrategy<DumpTask, DumpTaskExecutor> {

    @Autowired
    SrcDBInstanceService srcDBInstanceService;

    @Autowired
    DstDBInstanceService dstDBInstanceService;

    @Autowired
    PumaTaskService pumaTaskService;

    @Override
    public DumpTaskExecutor build(DumpTask task) {
        //根据Task创建TaskExecutor
        DumpTaskExecutor excutor;
        try {
            String pumaTaskName = task.getPumaTaskName();
            PumaTask pumaTask = pumaTaskService.findByName(pumaTaskName);
            String srcDBInstanceName = pumaTask.getSrcDBInstanceName();
            String dstDBInstanceName = task.getDstDBInstanceName();

            excutor = new DumpTaskExecutor(task);
            excutor.setSrcDBInstance(srcDBInstanceService.findByName(srcDBInstanceName));
            excutor.setDstDBInstance(dstDBInstanceService.findByName(dstDBInstanceName));

            return excutor;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Type getType() {
        return Type.DUMP;
    }

    @Override
    public SyncType getSyncType() {
        return SyncType.DUMP;
    }
}
