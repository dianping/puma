package com.dianping.puma.syncserver.job.executor.builder;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.entity.PumaServer;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.sync.model.config.PumaServerConfig;
import com.dianping.puma.core.sync.model.task.CatchupTask;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.container.TaskExecutionContainer;
import com.dianping.puma.syncserver.job.executor.CatchupTaskExecutor;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;
import com.dianping.puma.syncserver.service.PumaServerConfigService;

@Service("catchupTaskExecutorStrategy")
public class CatchupTaskExecutorStrategy implements TaskExecutorStrategy<CatchupTask, CatchupTaskExecutor> {
	/*
    @Autowired
    private PumaServerConfigService pumaServerConfigService;
    */
    @Autowired
    private TaskExecutionContainer taskExecutionContainer;

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

    @Override
    public CatchupTaskExecutor build(CatchupTask task) {
        //根据Task创建TaskExecutor
    	 String srcDBInstanceId = task.getSrcDBInstanceId();
         if(srcDBInstanceId == null){
             throw new IllegalArgumentException("SyncTask srcDBInstanceId  is null, maybe SyncTask with srcDBInstanceId["+srcDBInstanceId+"] is not setting.");
         }
         List<PumaTask> pumaTask = pumaTaskService.findBySrcDBInstanceId(srcDBInstanceId);
         
         if(pumaTask == null|| pumaTask.get(0) == null){
             throw new IllegalArgumentException("PumaTask is null, maybe PumaTask with srcDBInstanceId["+srcDBInstanceId+"] is not setting.");
         }
         PumaServer pumaServer = pumaServerService.find(pumaTask.get(0).getPumaServerId());
         if(pumaServer == null){
             throw new IllegalArgumentException("PumaServer is null, maybe PumaServer with PumaServerId["+pumaTask.get(0).getPumaServerId()+"] is not setting.");
         }
         
         String pumaServerHost = pumaServer.getHost();
         int pumaServerPort = pumaServer.getPort();
         
         String target = pumaTask.get(0).getId();
        //从taskContainer获取syncTaskExecutor
        SyncTaskExecutor syncTaskExecutor = (SyncTaskExecutor) taskExecutionContainer.get(Type.SYNC, task.getSyncTaskId());
        return new CatchupTaskExecutor(task, pumaServerHost, pumaServerPort, target, syncTaskExecutor);
    }
    @Override
    public Type getType() {
        return Type.CATCHUP;
    }

}
