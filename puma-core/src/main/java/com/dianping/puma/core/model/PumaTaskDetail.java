package com.dianping.puma.core.model;

import java.util.List;

import com.dianping.puma.core.model.state.PumaTaskState;

public class PumaTaskDetail {
	
	private PumaTaskState taskState;
	
	private List<ClientInfo> clientInfos;

	public void setTaskState(PumaTaskState taskState) {
		this.taskState = taskState;
	}

	public PumaTaskState getTaskState() {
		return taskState;
	}

	public void setClientInfos(List<ClientInfo> clientInfos) {
		this.clientInfos = clientInfos;
	}

	public List<ClientInfo> getClientInfos() {
		return clientInfos;
	}
}
