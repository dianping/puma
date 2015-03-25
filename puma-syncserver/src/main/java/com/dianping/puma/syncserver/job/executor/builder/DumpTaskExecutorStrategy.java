package com.dianping.puma.syncserver.job.executor.builder;

import java.io.IOException;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.DumpTask;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.holder.BinlogInfoHolder;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.model.state.DumpTaskState;
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
            PumaTask pumaTask = pumaTaskService.find(pumaTaskName);
            String srcDBInstanceName = pumaTask.getSrcDBInstanceName();
            String dstDBInstanceName = task.getDstDBInstanceName();

            excutor = new DumpTaskExecutor(task, new DumpTaskState());
            excutor.setSrcDBInstance(srcDBInstanceService.find(srcDBInstanceName));
            excutor.setDstDBInstance(dstDBInstanceService.find(dstDBInstanceName));

            DumpTaskState dumpTaskState = new DumpTaskState();
            dumpTaskState.setTaskName(task.getName());
            dumpTaskState.setStatus(Status.PREPARING);
            dumpTaskState.setBinlogInfo(task.getBinlogInfo());
            excutor.setTaskState(dumpTaskState);

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
