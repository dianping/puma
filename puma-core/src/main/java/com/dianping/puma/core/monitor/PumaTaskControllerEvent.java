package com.dianping.puma.core.monitor;

import com.dianping.puma.core.model.PumaTaskController;

public class PumaTaskControllerEvent extends PumaTaskEvent {

	private String taskId;

	private PumaTaskController controller;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public PumaTaskController getController() {
		return controller;
	}

	public void setController(PumaTaskController controller) {
		this.controller = controller;
	}
}
