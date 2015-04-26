package com.dianping.puma.core.model.event;

import java.util.Date;

public class Event {

	protected Date gmtCreate;

	public Event() {
		gmtCreate = new Date();
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}
}
