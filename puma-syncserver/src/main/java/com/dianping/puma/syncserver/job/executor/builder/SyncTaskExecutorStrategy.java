package com.dianping.puma.syncserver.job.executor.builder;

import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.*;
import com.dianping.puma.core.service.DstDBInstanceService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.syncserver.job.binlogmanage.MapDBBinlogManager;
import com.dianping.puma.syncserver.job.load.PooledLoader;
import com.dianping.puma.syncserver.job.transform.DefaultTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.service.PumaServerService;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.puma.syncserver.job.executor.SyncTaskExecutor;

import static com.google.common.base.Preconditions.*;

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

		// Sync task contains 3 parts:
		// 1. Puma task.
		// 2. Destination db instance.
		// 3. Source db instance.
		String pumaTaskName = task.getPumaTaskName();
		checkArgument(pumaTaskName != null, "Puma task name is null in sync task(%s).", name);
		PumaTask pumaTask = pumaTaskService.find(pumaTaskName);
		checkArgument(pumaTask != null, "Puma task is null in sync task(%s).", name);

		String dstDBInstanceName = task.getDstDBInstanceName();
		checkArgument(dstDBInstanceName != null, "Destination db instance name is null in sync task(%s).", name);
		DstDBInstance dstDBInstance = dstDBInstanceService.find(dstDBInstanceName);
		checkArgument(dstDBInstance != null, "Destination db instance is null in sync task(%s).", name);

		String srcDBInstanceName = pumaTask.getSrcDBInstanceName();
		checkArgument(srcDBInstanceName != null, "Source db instance name is null in sync task(%s).", name);
		SrcDBInstance srcDBInstance = srcDBInstanceService.find(srcDBInstanceName);
		checkArgument(srcDBInstance != null, "Source db instance is null in sync task(%s).", name);
		executor.setDstDBInstance(dstDBInstance);

		// Puma task contains 1 part:
		// 1. Puma server.
		String pumaServerName = pumaTask.getPumaServerName();
		checkArgument(pumaServerName != null, "Puma server name is null in sync task(%s).", name);
		PumaServer pumaServer = pumaServerService.find(pumaServerName);
		checkArgument(pumaServer != null, "Puma server is null in sync task(%s).", name);
		executor.setSrcDBInstance(srcDBInstance);

		// Setting Task.
		executor.setTask(task);

		// Setting Binlog manager.
		MapDBBinlogManager binlogInfoManager = new MapDBBinlogManager(task.getBinlogInfo());
		binlogInfoManager.setName(name);

		// Setting puma client connection settings.
		executor.setPumaTask(pumaTask);
		executor.setPumaServer(pumaServer);
		executor.setBinlogManager(binlogInfoManager);

		// Setting transformer.
		DefaultTransformer transformer = new DefaultTransformer();
		transformer.setName(name);
		MysqlMapping mysqlMapping = task.getMysqlMapping();
		checkArgument(mysqlMapping != null, "Mysql mapping is null in sync task(%s).", name);
		transformer.setMysqlMapping(mysqlMapping);
		executor.setTransformer(transformer);

		// Setting loader.
		PooledLoader loader = new PooledLoader();
		loader.setName(name);
		loader.setHost(dstDBInstance.getHost());
		loader.setUsername(dstDBInstance.getUsername());
		loader.setPassword(dstDBInstance.getPassword());
		loader.setBinlogManager(binlogInfoManager);
		executor.setLoader(loader);

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
