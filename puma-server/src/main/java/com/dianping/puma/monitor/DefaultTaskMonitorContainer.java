package com.dianping.puma.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;


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

	public void execute() {
		for (Map.Entry<String, AbstractTaskMonitor> taskMonitorEntry : taskMonitors.entrySet()) {
			taskMonitorEntry.getValue().execute(executorService);
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
		this.execute();
	}
	
	private void constructTaskMonitor() {
		constructClientInfoTaskMonitor();
		constructClientIpTaskMonitor();
		constructServerInfoTaskMonitor();
		constructSyncProcessTaskMonitor();
	}

	private void constructClientInfoTaskMonitor() {
		AbstractTaskMonitor taskMonitor = new ClientInfoTaskMonitor(0, TimeUnit.MILLISECONDS);
		this.register(ClientInfoTaskMonitor.CLIENTINFO_INTERVAL_NAME, taskMonitor);
	}

	private void constructClientIpTaskMonitor() {
		AbstractTaskMonitor taskMonitor = new ClientIpTaskMonitor(0, TimeUnit.MILLISECONDS);
		this.register(ClientIpTaskMonitor.CLIENTIP_INTERVAL_NAME, taskMonitor);
	}

	private void constructServerInfoTaskMonitor() {
		AbstractTaskMonitor taskMonitor = new ServerInfoTaskMonitor(0, TimeUnit.MILLISECONDS);
		this.register(ServerInfoTaskMonitor.SERVERINFO_INTERVAL_NAME, taskMonitor);
	}

	private void constructSyncProcessTaskMonitor() {
		SyncProcessTaskMonitor taskMonitor = new SyncProcessTaskMonitor(0, TimeUnit.MILLISECONDS);
		this.register(SyncProcessTaskMonitor.SYNCPROCESS_INTERVAL_NAME, taskMonitor);
	}

}
