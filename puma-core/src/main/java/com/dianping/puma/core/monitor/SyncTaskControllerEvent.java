package com.dianping.puma.core.monitor;

import com.dianping.puma.core.constant.ActionController;

public class SyncTaskControllerEvent extends SyncTaskEvent {

	private String taskName;

	private ActionController controller;

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskName() {
		return taskName;
	}

	public ActionController getController() {
		return controller;
	}

	public void setController(ActionController controller) {
		this.controller = controller;
	}
}
