package com.dianping.puma.core.model.state;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.BinlogStat;

import java.util.Date;

public abstract class TaskState {

	private Date gmtUpdate;

	private String taskName;

	private String detail;

	private Status status;

	private String strStatus;
	
	private ActionController controller;

	private BinlogInfo binlogInfo;

	private BinlogStat binlogStat;

	public TaskState() {
		gmtUpdate = new Date();
	}

	public Date getGmtUpdate() {
		return gmtUpdate;
	}

	public void setGmtUpdate(Date gmtUpdate) {
		this.gmtUpdate = gmtUpdate;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public ActionController getController() {
		return controller;
	}

	public void setController(ActionController controller) {
		this.controller = controller;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public BinlogStat getBinlogStat() {
		return binlogStat;
	}

	public void setBinlogStat(BinlogStat binlogStat) {
		this.binlogStat = binlogStat;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
		this.strStatus = status.getDesc();
	}

	public String getStrStatus() {
		return strStatus;
	}
	
}
