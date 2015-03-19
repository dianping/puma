package com.dianping.puma.core.monitor;

import com.dianping.puma.core.constant.Controller;

public class SyncTaskControllerEvent extends SyncTaskEvent {

	private String taskName;

	private Controller controller;

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskName() {
		return taskName;
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}
}
