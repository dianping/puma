package com.dianping.puma.core.model.event;

import java.util.Date;

public class Event {

	protected Date gmtCreate;

	protected String name;

	public Event() {
		gmtCreate = new Date();
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
