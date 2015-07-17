package com.dianping.puma.biz.entity;

import com.dianping.puma.core.constant.ActionController;

public class PumaTaskServerEntity {

	private int id;

	private int taskId;

	private int serverId;

	private ActionController actionController;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public ActionController getActionController() {
		return actionController;
	}

	public void setActionController(ActionController actionController) {
		this.actionController = actionController;
	}
}
