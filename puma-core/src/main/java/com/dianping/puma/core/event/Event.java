package com.dianping.puma.core.event;

import com.dianping.puma.core.model.BinlogInfo;

import java.io.Serializable;

public abstract class Event implements Serializable {

	private static final long serialVersionUID = 7986284681273254505L;

	private long seq;
	public void setSeq(long seq) {
		this.seq = seq;
	}
	public long getSeq() {
		return seq;
	}

	public abstract BinlogInfo getBinlogInfo();

	public abstract EventType getEventType();
}
