package com.dianping.puma.monitor.todo;

import java.util.Map;

public interface TaskMonitorContainer {

	public void setTaskMonitors(Map<String, AbstractTaskMonitor> taskMonitors);

	public Map<String, AbstractTaskMonitor> getTaskMonitors();

	public void execute();

	public void register(String name, AbstractTaskMonitor taskMonitor);

	public void remove(AbstractTaskMonitor taskMonitor);

	public void start();
}
