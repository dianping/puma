package com.dianping.puma.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.dianping.puma.config.ServerLionCommonKey;

@Component("taskMonitorContainer")
public class DefaultTaskMonitorContainer implements TaskMonitorContainer {
	
	private Map<String, AbstractTaskMonitor> taskMonitors = new HashMap<String, AbstractTaskMonitor>();

	private ScheduledExecutorService executorService = null;
	
	private static final int MAXTHREADCOUNT = 5;
	

	public void initScheduledExecutorService(int maxThreadCount) {
		if (this.getTaskMonitors().size() > maxThreadCount) {
			executorService = Executors.newScheduledThreadPool(maxThreadCount);
		} else {
			executorService = Executors.newScheduledThreadPool(this.getTaskMonitors().size());
		}
	}

	public void setTaskMonitors(Map<String, AbstractTaskMonitor> taskMonitors) {
		this.taskMonitors = taskMonitors;
	}

	public Map<String, AbstractTaskMonitor> getTaskMonitors() {
		return taskMonitors;
	}

	public void execute(ScheduledExecutorService executor) {
		for (Map.Entry<String, AbstractTaskMonitor> taskMonitorEntry : taskMonitors.entrySet()) {
			taskMonitorEntry.getValue().execute(executor);
		}
	}

	public void register(String name, AbstractTaskMonitor taskMonitor) {
		taskMonitors.put(name, taskMonitor);
	}

	public void remove(AbstractTaskMonitor taskMonitor) {
		taskMonitors.remove(taskMonitor);
	}
	
	@PostConstruct
	@Override
	public void start()
	{
		constructTaskMonitor();
		initScheduledExecutorService(MAXTHREADCOUNT);
		this.execute(executorService);
	}
	
	private void constructTaskMonitor() {
		constructSequenceTaskMonitor(ServerLionCommonKey.SEQ_INTERVAL_NAME);
		constructClientIpTaskMonitor(ServerLionCommonKey.CLIENTIP_INTERVAL_NAME);
		constructServerInfoTaskMonitor(ServerLionCommonKey.SERVERINFO_INTERVAL_NAME);
		constructSyncProcessTaskMonitor(ServerLionCommonKey.SYNCPROCESS_INTERVAL_NAME);
	}

	private void constructSequenceTaskMonitor(String key) {
		AbstractTaskMonitor taskMonitor = new ClientInfoTaskMonitor(0, TimeUnit.MILLISECONDS);
		this.register(key, taskMonitor);
	}

	private void constructClientIpTaskMonitor(String key) {
		AbstractTaskMonitor taskMonitor = new ClientIpTaskMonitor(0, TimeUnit.MILLISECONDS);
		this.register(key, taskMonitor);
	}

	private void constructServerInfoTaskMonitor(String key) {
		AbstractTaskMonitor taskMonitor = new ServerInfoTaskMonitor(0, TimeUnit.MILLISECONDS);
		this.register(key, taskMonitor);
	}

	private void constructSyncProcessTaskMonitor(String key) {
		SyncProcessTaskMonitor taskMonitor = new SyncProcessTaskMonitor(0, TimeUnit.MILLISECONDS);
		this.register(key, taskMonitor);
	}

}
