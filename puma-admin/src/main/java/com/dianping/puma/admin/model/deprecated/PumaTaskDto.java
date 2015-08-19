package com.dianping.puma.admin.model.deprecated;

import java.util.List;
import java.util.Map;

public class PumaTaskDto {

	private Map<String, List<String>> targets;

	private List<String> hosts;

	private long beginTime;

	public Map<String, List<String>> getTargets() {
		return targets;
	}

	public void setTargets(Map<String, List<String>> targets) {
		this.targets = targets;
	}

	public List<String> getHosts() {
		return hosts;
	}

	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}
}
