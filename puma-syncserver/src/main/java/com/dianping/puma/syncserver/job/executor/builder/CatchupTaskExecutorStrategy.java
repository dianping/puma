package com.dianping.puma.syncserver.job.executor.builder;

import com.dianping.puma.biz.entity.TaskStateEntity;
import com.dianping.puma.biz.entity.old.*;
import com.dianping.puma.biz.service.DstDBInstanceService;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.biz.service.PumaTaskService;
import com.dianping.puma.biz.service.SrcDBInstanceService;
import com.dianping.puma.biz.sync.model.task.Type;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.storage.holder.BinlogInfoHolder;
import com.dianping.puma.syncserver.job.container.TaskExecutorContainer;
import com.dianping.puma.syncserver.job.executor.CatchupTaskExecutor;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("catchupTaskExecutorStrategy")
public class CatchupTaskExecutorStrategy implements TaskExecutorStrategy<CatchupTask, CatchupTaskExecutor> {
    /*
     @Autowired
    private PumaServerConfigService pumaServerConfigService;
    */
    @Autowired
    private TaskExecutorContainer taskExecutorContainer;

    @Autowired
    private BinlogInfoHolder binlogInfoHolder;


    /*
     @Override
     public CatchupTaskExecutor build(CatchupTask task) {
          //根据Task创建TaskExecutor
          String srcMysqlName = task.getSrcMysqlName();
          PumaServerConfig pumaServerConfig = pumaServerConfigService.find(srcMysqlName);
          String pumaServerHostAndPort = pumaServerConfig.getHosts().get(0);
          String pumaServerHost = pumaServerHostAndPort;
          int pumaServerPort = 80;
          if (StringUtils.contains(pumaServerHostAndPort, ':')) {
                String[] splits = pumaServerHostAndPort.split(":");
                pumaServerHost = splits[0];
                pumaServerPort = Integer.parseInt(splits[1]);
          }
          String target = pumaServerConfig.getTarget();
          //从taskContainer获取syncTaskExecutor
          SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) taskExecutionContainer.get(Type.SYNC, task.getSyncTaskId());
          return new CatchupTaskExecutor(task, pumaServerHost, pumaServerPort, target, syncTaskExecutor);
     }
     */
    @Autowired
    private PumaServerService pumaServerService;

    @Autowired
    private PumaTaskService pumaTaskService;

    @Autowired
    private DstDBInstanceService dstDBInstanceService;

    @Autowired
    SrcDBInstanceService srcDBInstanceService;

    @Override
    public CatchupTaskExecutor build(CatchupTask task) {
        //根据Task创建TaskExecutor

        String pumaTaskName = task.getPumaTaskName();
        //String srcDBInstanceId = task.getSrcDBInstanceId();
        if (pumaTaskName == null) {
            throw new IllegalArgumentException(
                    "SyncTask srcDBInstanceId  is null, maybe SyncTask with srcDBInstanceId[" + pumaTaskName
                            + "] is not setting.");
        }
        PumaTask pumaTask = pumaTaskService.find(pumaTaskName);

        if (pumaTask == null) {
            throw new IllegalArgumentException(
                    "PumaTask is null, maybe PumaTask with srcDBInstanceId[" + pumaTaskName + "] is not setting.");
        }
        PumaServer pumaServer = pumaServerService.find(pumaTask.getPumaServerName());
        if (pumaServer == null) {
            throw new IllegalArgumentException(
                    "PumaServer is null, maybe PumaServer with PumaServerId[" + pumaTask.getPumaServerName()
                            + "] is not setting.");
        }

        String pumaServerHost = pumaServer.getHost();
        int pumaServerPort = pumaServer.getPort();

        String target = pumaTask.getName();
        //从taskContainer获取syncTaskExecutor
        SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) taskExecutorContainer
                .get(task.getName());

        DstDBInstance dstDBInstance = dstDBInstanceService.find(task.getDstDBInstanceName());

        SrcDBInstance srcDBInstance = srcDBInstanceService.find(pumaTask.getSrcDBInstanceName());
        task.setPumaClientServerId(srcDBInstance.getServerId());

        CatchupTaskExecutor executor = new CatchupTaskExecutor(task, pumaServerHost, pumaServerPort, target, syncTaskExecutor, dstDBInstance);

        TaskStateEntity catchupTaskState = new TaskStateEntity();
        catchupTaskState.setTaskName(task.getName());
        catchupTaskState.setStatus(Status.PREPARING);

        return executor;
    }

    @Override
    public Type getType() {
        return Type.CATCHUP;
    }

    @Override
    public SyncType getSyncType() {
        return SyncType.CATCHUP;
    }
}
