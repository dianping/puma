package com.dianping.puma.api.manager;

import com.dianping.puma.core.model.BinlogInfo;

public class PositionManager {

	private String name;

	private BinlogInfo binlogInfo;

	public PositionManager() {
	}

	public void init() {

	}

	public BinlogInfo next() {
		return new BinlogInfo();
	}

	public void save(BinlogInfo binlogInfo) {

	}

	public void setName(String name) {
		this.name = name;
	}
}
