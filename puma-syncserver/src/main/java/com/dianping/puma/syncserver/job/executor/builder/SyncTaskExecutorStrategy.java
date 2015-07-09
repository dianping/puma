package com.dianping.puma.syncserver.job.executor.builder;

import com.dianping.puma.biz.entity.old.*;
import com.dianping.puma.biz.service.DstDBInstanceService;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.biz.service.PumaTaskService;
import com.dianping.puma.biz.service.SrcDBInstanceService;
import com.dianping.puma.biz.sync.model.mapping.MysqlMapping;
import com.dianping.puma.biz.sync.model.task.Type;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.syncserver.job.binlogmanage.MapDBBinlogManager;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.job.executor.exception.TEException;
import com.dianping.puma.syncserver.job.transform.DefaultTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("syncTaskExecutorStrategy")
public class SyncTaskExecutorStrategy implements TaskExecutorStrategy<SyncTask, SyncTaskExecutor> {

    @Autowired
    private PumaTaskService pumaTaskService;

    @Autowired
    private PumaServerService pumaServerService;

    @Autowired
    DstDBInstanceService dstDBInstanceService;

    @Autowired
    SrcDBInstanceService srcDBInstanceService;

    @Override
    public SyncTaskExecutor build(SyncTask task) {
        SyncTaskExecutor executor = new SyncTaskExecutor();
        String name = task.getName();

        // Binlog manager setting.
        MapDBBinlogManager binlogInfoManager = new MapDBBinlogManager(SubscribeConstant.SEQ_FROM_BINLOGINFO, task.getBinlogInfo());
        binlogInfoManager.setName(name);

        // Transformer setting.
        DefaultTransformer transformer = new DefaultTransformer();
        transformer.setName(name);
        MysqlMapping mysqlMapping = task.getMysqlMapping();
        if (mysqlMapping == null) {
            throw new TEException(-1, String.format("Mysql mapping is null in sync task(%s).", name));
        }
        transformer.setMysqlMapping(mysqlMapping);
        executor.setTransformer(transformer);

        // Loader setting.
        String dstDBInstanceName = task.getDstDBInstanceName();
        DstDBInstance dstDBInstance = dstDBInstanceService.find(dstDBInstanceName);
        if (dstDBInstance == null) {
            throw new TEException(-1, String.format("Destination db instance is null in sync task(%s).", name));
        }


        // Executor setting.
        executor.setTask(task);
        executor.setBinlogManager(binlogInfoManager);
        executor.setTransformer(transformer);

        String pumaTaskName = task.getPumaTaskName();
        PumaTask pumaTask = pumaTaskService.find(pumaTaskName);
        if (pumaTask == null) {
            throw new TEException(-1, String.format("Puma task is null in sync task(%s).", name));
        }

        executor.setPumaTaskName(pumaTask.getName());

        String pumaServerName = pumaTask.getPumaServerName();
        PumaServer pumaServer = pumaServerService.find(pumaServerName);
        if (pumaServer == null) {
            throw new TEException(-1, String.format("Puma server is null in sync task(%s).", name));
        }

        executor.setPumaServerHost(pumaServer.getHost());
        executor.setPumaServerPort(pumaServer.getPort());

        String srcDBInstanceName = pumaTask.getSrcDBInstanceName();
        SrcDBInstance srcDBInstance = srcDBInstanceService.find(srcDBInstanceName);
        if (srcDBInstance == null) {
            throw new TEException(-1, String.format("Source db instance is null in sync task(%s).", name));
        }

        executor.setPumaClientServerName(task.getPumaClientName());
        executor.setPumaClientServerId(srcDBInstance.getServerId());

        return executor;
    }

    @Override
    public Type getType() {
        return Type.SYNC;
    }

    @Override
    public SyncType getSyncType() {
        return SyncType.SYNC;
    }
}
