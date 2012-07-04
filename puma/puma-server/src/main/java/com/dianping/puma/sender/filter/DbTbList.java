package com.dianping.puma.sender.filter;

import java.util.List;

public class DbTbList {
	private String			dbName;
	private List<String>	tbNameList;

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public List<String> getTbNameList() {
		return tbNameList;
	}

	public void setTbNameList(List<String> tbNameList) {
		this.tbNameList = tbNameList;
	}
}