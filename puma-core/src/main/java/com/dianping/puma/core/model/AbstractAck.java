package com.dianping.puma.core.model;

import java.io.Serializable;
import java.util.Date;

public abstract class AbstractAck implements Serializable{

	private static final long serialVersionUID = -6032990589569319213L;
	
	private Date createDate;
	
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getCreateDate() {
		return createDate;
	}
}
