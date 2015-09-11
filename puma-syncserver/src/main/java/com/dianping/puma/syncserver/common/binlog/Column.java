package com.dianping.puma.syncserver.common.binlog;

public class Column {

	private boolean pk;

	private Object oldValue;

	private Object newValue;

	public Column() {

	}

	public Column(boolean pk, Object oldValue, Object newValue) {
		this.pk = pk;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

	public boolean isPk() {
		return pk;
	}

	public void setPk(boolean pk) {
		this.pk = pk;
	}

	public Object getNewValue() {
		return newValue;
	}

	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}
}
