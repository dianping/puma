package com.dianping.puma.biz.event.entity;

import com.dianping.puma.core.constant.ActionController;

public class TaskControllerEvent extends TaskEvent {

	String taskName;

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
