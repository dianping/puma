package com.dianping.puma.monitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.puma.config.ServerLionCommonKey;

@Component("taskMonitorManager")
public class TaskMonitorManager {

	private static final Logger LOG = LoggerFactory.getLogger(TaskMonitorManager.class);

	private ScheduledExecutorService executorService = null;

	private static final int MAXTHREADCOUNT = 5;

	private long seqInterval;

	private long clientIpInterval;

	private long serverInfoInterval;

	private long syncProcessInterval;

	private int syncProcessDfileNum;

	TaskMonitorContainer taskMonitorContainer = null;

	public void initScheduledExecutorService(int maxThreadCount) {
		if (taskMonitorContainer.getTaskMonitors().size() > maxThreadCount) {
			executorService = Executors.newScheduledThreadPool(maxThreadCount);
		} else {
			executorService = Executors.newScheduledThreadPool(taskMonitorContainer.getTaskMonitors().size());
		}
	}

	@PostConstruct
	public void start() {
		init();
		initTaskMonitor();
	}

	public void init() {
		taskMonitorContainer = new DefaultTaskMonitorContainer();
		initConfig();
		ConfigCache.getInstance().addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (ServerLionCommonKey.SEQ_INTERVAL_NAME.equals(key)) {
					executorService.shutdownNow();
					seqInterval = getInterval(ServerLionCommonKey.SEQ_INTERVAL_NAME);
				} else if (ServerLionCommonKey.CLIENTIP_INTERVAL_NAME.equals(key)) {
					executorService.shutdownNow();
					clientIpInterval = getInterval(ServerLionCommonKey.CLIENTIP_INTERVAL_NAME);
				} else if (ServerLionCommonKey.SERVERINFO_INTERVAL_NAME.equals(key)) {
					executorService.shutdownNow();
					serverInfoInterval = getInterval(ServerLionCommonKey.SERVERINFO_INTERVAL_NAME);
				} else if (ServerLionCommonKey.SYNCPROCESS_INTERVAL_NAME.equals(key)) {
					executorService.shutdownNow();
					syncProcessInterval = getInterval(ServerLionCommonKey.SYNCPROCESS_INTERVAL_NAME);
				} else if (ServerLionCommonKey.SYNCPROCESS_DIFF_FILE_NUM.equals(key)) {
					syncProcessDfileNum = getNumThreshold(ServerLionCommonKey.SYNCPROCESS_DIFF_FILE_NUM);
					if (taskMonitorContainer.getTaskMonitors().containsKey(
							ServerLionCommonKey.SYNCPROCESS_INTERVAL_NAME)) {
						SyncProcessTaskMonitor taskMonitor = (SyncProcessTaskMonitor) taskMonitorContainer
								.getTaskMonitors().get(ServerLionCommonKey.SYNCPROCESS_INTERVAL_NAME);
						taskMonitor.setNumThreshold(syncProcessDfileNum);
					}
				}
				try {
					while (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
						Log.info("Scheduled task monitor await Termination.");
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				executorService = null;
				initTaskMonitor();
			}
		});
	}

	private void initConfig() {
		seqInterval = getInterval(ServerLionCommonKey.SEQ_INTERVAL_NAME);
		clientIpInterval = getInterval(ServerLionCommonKey.CLIENTIP_INTERVAL_NAME);
		serverInfoInterval = getInterval(ServerLionCommonKey.SERVERINFO_INTERVAL_NAME);
		syncProcessInterval = getInterval(ServerLionCommonKey.SYNCPROCESS_INTERVAL_NAME);
		syncProcessDfileNum = getNumThreshold(ServerLionCommonKey.SYNCPROCESS_DIFF_FILE_NUM);
	}

	private void initTaskMonitor() {
		constructTaskMonitor();
		initScheduledExecutorService(MAXTHREADCOUNT);
		taskMonitorContainer.setTaskMonitorExecutor(executorService);
		taskMonitorContainer.execute();
	}

	private void constructTaskMonitor() {
		constructSequenceTaskMonitor(ServerLionCommonKey.SEQ_INTERVAL_NAME, seqInterval);
		constructClientIpTaskMonitor(ServerLionCommonKey.CLIENTIP_INTERVAL_NAME, clientIpInterval);
		constructServerInfoTaskMonitor(ServerLionCommonKey.SERVERINFO_INTERVAL_NAME, serverInfoInterval);
		constructSyncProcessTaskMonitor(ServerLionCommonKey.SYNCPROCESS_INTERVAL_NAME, syncProcessInterval,
				syncProcessDfileNum);
	}

	private void constructSequenceTaskMonitor(String key, long delay) {
		AbstractTaskMonitor taskMonitor = new ClientInfoTaskMonitor(0, delay, TimeUnit.MILLISECONDS);
		taskMonitorContainer.register(key, taskMonitor);
	}

	private void constructClientIpTaskMonitor(String key, long delay) {
		AbstractTaskMonitor taskMonitor = new ClientIpTaskMonitor(0, delay, TimeUnit.MILLISECONDS);
		taskMonitorContainer.register(key, taskMonitor);
	}

	private void constructServerInfoTaskMonitor(String key, long delay) {
		AbstractTaskMonitor taskMonitor = new ServerInfoTaskMonitor(0, delay, TimeUnit.MILLISECONDS);
		taskMonitorContainer.register(key, taskMonitor);
	}

	private void constructSyncProcessTaskMonitor(String key, long delay, int threshold) {
		SyncProcessTaskMonitor taskMonitor = new SyncProcessTaskMonitor(0, delay, TimeUnit.MILLISECONDS);
		taskMonitor.setNumThreshold(threshold);
		taskMonitorContainer.register(key, taskMonitor);
	}

	protected long getInterval(String intervalName) {
		long interval = 60000;
		try {
			Long temp = ConfigCache.getInstance().getLongProperty(intervalName);
			if (temp != null) {
				interval = temp.longValue();
			}
		} catch (LionException e) {
			LOG.error(e.getMessage(), e);
		}
		return interval;
	}

	private int getNumThreshold(String keyName) {
		int numFile = 2;
		try {
			Integer temp = ConfigCache.getInstance().getIntProperty(keyName);
			if (temp != null) {
				numFile = temp.intValue();
			}
		} catch (LionException e) {
			LOG.error(e.getMessage(), e);
		}
		return numFile;
	}

}
