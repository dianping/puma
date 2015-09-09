package com.dianping.puma.biz.entity;

import java.util.Date;
import java.util.Map;

public class CheckTaskEntity {

	private int id;

	private String srcDatabase;

	private String dstDatabase;

	private String srcTable;

	private String dstTable;

	private Map<String, String> columnMapping;

	private Date initTime;

	private Date nextTime;

	private boolean running;

	private Date updateTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSrcDatabase() {
		return srcDatabase;
	}

	public void setSrcDatabase(String srcDatabase) {
		this.srcDatabase = srcDatabase;
	}

	public String getDstDatabase() {
		return dstDatabase;
	}

	public void setDstDatabase(String dstDatabase) {
		this.dstDatabase = dstDatabase;
	}

	public String getSrcTable() {
		return srcTable;
	}

	public void setSrcTable(String srcTable) {
		this.srcTable = srcTable;
	}

	public String getDstTable() {
		return dstTable;
	}

	public void setDstTable(String dstTable) {
		this.dstTable = dstTable;
	}

	public Map<String, String> getColumnMapping() {
		return columnMapping;
	}

	public void setColumnMapping(Map<String, String> columnMapping) {
		this.columnMapping = columnMapping;
	}

	public Date getInitTime() {
		return initTime;
	}

	public void setInitTime(Date initTime) {
		this.initTime = initTime;
	}

	public Date getNextTime() {
		return nextTime;
	}

	public void setNextTime(Date nextTime) {
		this.nextTime = nextTime;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
}
