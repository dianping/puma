package com.dianping.puma.syncserver.job.executor.builder;

import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.*;
import com.dianping.puma.core.storage.holder.BinlogInfoHolder;
import com.dianping.puma.core.monitor.NotifyService;
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

@Service("syncTaskExecutorStrategy")
public class SyncTaskExecutorStrategy implements TaskExecutorStrategy<SyncTask, SyncTaskExecutor> {

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
		SyncTaskExecutor executor = new SyncTaskExecutor();

		String name = task.getName();

		MapDBBinlogManager binlogInfoManager = new MapDBBinlogManager();

		// Client connection settings.
		String pumaTaskName = task.getPumaTaskName();
		PumaTask pumaTask = pumaTaskService.find(pumaTaskName);
		if (pumaTask == null) {
			throw new IllegalArgumentException(String.format("Puma task is null in sync task(%s).", name));
		}
		executor.setTarget(pumaTask.getName());
		PumaServer pumaServer = pumaServerService.find(pumaTask.getPumaServerName());
		if (pumaServer == null) {
			throw new IllegalArgumentException(String.format("Puma server is null in sync task(%s).", name));
		}
		executor.setPumaServerHost(pumaServer.getHost());
		executor.setPumaServerPort(pumaServer.getPort());
		executor.setBinlogManager(binlogInfoManager);

		// Transformer.
		DefaultTransformer transformer = new DefaultTransformer();
		transformer.setName(name);
		MysqlMapping mysqlMapping = task.getMysqlMapping();
		transformer.setMysqlMapping(mysqlMapping);
		executor.setTransformer(transformer);

		// Loader.
		PooledLoader loader = new PooledLoader();
		loader.setName(name);
		DstDBInstance dstDBInstance = dstDBInstanceService.find(task.getDstDBInstanceName());
		if (dstDBInstance == null) {
			throw new IllegalArgumentException(String.format("Destination db instance is null in sync task(%s).", name));
		}
		loader.setHost(dstDBInstance.getHost());
		loader.setUsername(dstDBInstance.getUsername());
		loader.setPassword(dstDBInstance.getPassword());
		loader.setBinlogManager(binlogInfoManager);
		executor.setLoader(loader);

		return executor;

		/*
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

		SrcDBInstance srcDBInstance = srcDBInstanceService.find(pumaTask.getSrcDBInstanceName());
		task.setPumaClientServerId(srcDBInstance.getServerId());

		DstDBInstance dstDBInstance = dstDBInstanceService.find(task.getDstDBInstanceName());

		SyncTaskExecutor executor = new SyncTaskExecutor(task, pumaServerHost, pumaServerPort, target, dstDBInstance);
		executor.setBinlogInfoHolder(binlogInfoHolder);
		executor.setNotifyService(notifyService);

		SyncTaskState syncTaskState = new SyncTaskState();
		syncTaskState.setTaskName(task.getName());
		syncTaskState.setStatus(Status.PREPARING);

		BinlogInfo binlogInfo = binlogInfoHolder.getBinlogInfo(task.getName());
		if (binlogInfo == null) {
			binlogInfo = task.getBinlogInfo();
		}
		syncTaskState.setBinlogInfo(binlogInfo);

		executor.setTaskState(syncTaskState);

		return executor;*/
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
