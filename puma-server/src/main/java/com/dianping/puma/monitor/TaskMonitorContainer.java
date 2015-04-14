package com.dianping.puma.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public abstract class TaskMonitorContainer {

	private Map<String, AbstractTaskMonitor> taskMonitors = new HashMap<String, AbstractTaskMonitor>();

	public void setTaskMonitors(Map<String, AbstractTaskMonitor> taskMonitors) {
		this.taskMonitors = taskMonitors;
	}

	public Map<String, AbstractTaskMonitor> getTaskMonitors() {
		return taskMonitors;
	}

	public void execute() {
		for (Map.Entry<String, AbstractTaskMonitor> taskMonitorEntry : taskMonitors.entrySet()) {
			taskMonitorEntry.getValue().execute();
		}
	}

	public void register(String name, AbstractTaskMonitor taskMonitor) {
		taskMonitors.put(name, taskMonitor);
	}

	public void setTaskMonitorExecutor(ScheduledExecutorService executorService) {
		for (Map.Entry<String, AbstractTaskMonitor> taskMonitorEntry : taskMonitors.entrySet()) {
			taskMonitorEntry.getValue().setExecutor(executorService);
		}
	}

	public void remove(AbstractTaskMonitor taskMonitor) {
		taskMonitors.remove(taskMonitor);
	}

}
