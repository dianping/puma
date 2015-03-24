package com.dianping.puma.syncserver.job.executor.builder;

import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.*;
import com.dianping.puma.core.holder.BinlogInfoHolder;
import com.dianping.puma.core.holder.impl.DefaultBinlogInfoHolder;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.service.DstDBInstanceService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;

@Service("syncTaskExecutorStrategy")
public class SyncTaskExecutorStrategy implements TaskExecutorStrategy<SyncTask, SyncTaskExecutor> {
	/*
	@Autowired
    private PumaServerConfigService pumaServerConfigService;

    @Override
    public SyncTaskExecutor build(SyncTask task) {
        //根据Task创建TaskExecutor
        String srcMysqlName = task.getSrcMysqlName();
        PumaServerConfig pumaServerConfig = pumaServerConfigService.find(srcMysqlName);
        if(pumaServerConfig==null){
            throw new IllegalArgumentException("PumaServer is null, maybe PumaServer with srcMysqlName["+srcMysqlName+"] is not setting.");
        }
        String pumaServerHostAndPort = pumaServerConfig.getHosts().get(0);
        String pumaServerHost = pumaServerHostAndPort;
        int pumaServerPort = 80;
        if (StringUtils.contains(pumaServerHostAndPort, ':')) {
            String[] splits = pumaServerHostAndPort.split(":");
            pumaServerHost = splits[0];
            pumaServerPort = Integer.parseInt(splits[1]);
        }
        String target = pumaServerConfig.getTarget();

        SyncTaskExecutor excutor = new SyncTaskExecutor(task, pumaServerHost, pumaServerPort, target);
        return excutor;
    }
	*/
    @Autowired
    private PumaServerService pumaServerService;
    
    @Autowired
    private PumaTaskService pumaTaskService;

    @Autowired
    SrcDBInstanceService srcDBInstanceService;

    @Autowired
    DstDBInstanceService dstDBInstanceService;

    @Autowired
    BinlogInfoHolder binlogInfoHolder;
    
    @Autowired
    NotifyService notifyService;
    
    @Override
    public SyncTaskExecutor build(SyncTask task) {
        //根据Task创建TaskExecutor

        String pumaTaskName = task.getPumaTaskName();
        //String srcDBInstanceId = task.getSrcDBInstanceId();
        if(pumaTaskName == null){
            throw new IllegalArgumentException("SyncTask srcDBInstanceId  is null, maybe SyncTask with srcDBInstanceId["+pumaTaskName+"] is not setting.");
        }
        PumaTask pumaTask = pumaTaskService.find(pumaTaskName);
        
        if(pumaTask == null){
            throw new IllegalArgumentException("PumaTask is null, maybe PumaTask with srcDBInstanceId["+pumaTaskName+"] is not setting.");
        }
        PumaServer pumaServer = pumaServerService.find(pumaTask.getPumaServerName());
        if(pumaServer == null){
            throw new IllegalArgumentException("PumaServer is null, maybe PumaServer with PumaServerId["+pumaTask.getPumaServerName()+"] is not setting.");
        }
        
        String pumaServerHost = pumaServer.getHost();
        int pumaServerPort = pumaServer.getPort();
        
        String target = pumaTask.getName();

        SrcDBInstance srcDBInstance = srcDBInstanceService.find(pumaTask.getSrcDBInstanceName());
        task.setPumaClientServerId(srcDBInstance.getServerId());

        DstDBInstance dstDBInstance = dstDBInstanceService.find(task.getDstDBInstanceName());

        SyncTaskExecutor excutor = new SyncTaskExecutor(task, new SyncTaskState(), pumaServerHost, pumaServerPort, target, dstDBInstance);
        excutor.setBinlogInfoHolder(binlogInfoHolder);
        excutor.setNotifyService(notifyService);
        return excutor;
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
