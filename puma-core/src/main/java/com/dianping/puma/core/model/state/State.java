package com.dianping.puma.core.model.state;

import java.util.Date;

public abstract class State {

	private Date gmtCreate;

	private String name;

	public State() {
		gmtCreate = new Date();
	}

	public State(String name) {
		this.name = name;
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
