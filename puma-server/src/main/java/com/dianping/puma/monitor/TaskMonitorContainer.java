package com.dianping.puma.monitor;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public interface TaskMonitorContainer {

	public void setTaskMonitors(Map<String, AbstractTaskMonitor> taskMonitors);

	public Map<String, AbstractTaskMonitor> getTaskMonitors();

	public void execute();

	public void register(String name, AbstractTaskMonitor taskMonitor);

	public void remove(AbstractTaskMonitor taskMonitor);

	public void start();
}
