package com.dianping.puma.biz.entity.sync;

import com.dianping.puma.core.model.BinlogInfo;

public class SyncTaskEntity extends BaseTaskEntity {

	private int id;

	private String name;

	private BinlogInfo binlogInfo;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}
}
