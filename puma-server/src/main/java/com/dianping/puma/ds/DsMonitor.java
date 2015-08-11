package com.dianping.puma.ds;

public interface DsMonitor {

	public Cluster getCluster(String clusterName);

	public void addListener(String clusterName, DsMonitorListener listener);

	public void removeListener(String clusterName);
}
