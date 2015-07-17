package com.dianping.puma.biz.entity;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.TableSet;

import java.util.Date;
import java.util.List;

public class PumaTaskEntity {

	private int id;

	private String name;
	private int preservedDay;
	private TableSet tableSet;
	private List<PumaServerEntity> pumaServers;
	private String jdbcRef;


	private BinlogInfo binlogInfo;

	private SrcDbEntity preferSrcDb;

	private List<SrcDbEntity> backUpSrcDbs;

	private Date UpdateTime;

	private ActionController actionController;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPreservedDay() {
		return preservedDay;
	}

	public void setPreservedDay(int preservedDay) {
		this.preservedDay = preservedDay;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TableSet getTableSet() {
		return tableSet;
	}

	public SrcDbEntity getPreferSrcDb() {
		return preferSrcDb;
	}

	public void setPreferSrcDb(SrcDbEntity preferSrcDb) {
		this.preferSrcDb = preferSrcDb;
	}

	public List<SrcDbEntity> getBackUpSrcDbs() {
		return backUpSrcDbs;
	}

	public void setBackUpSrcDbs(List<SrcDbEntity> backUpSrcDbs) {
		this.backUpSrcDbs = backUpSrcDbs;
	}

	public void setPumaServers(List<PumaServerEntity> pumaServers) {
		this.pumaServers = pumaServers;
	}

	public void setTableSet(TableSet tableSet) {
		this.tableSet = tableSet;
	}

	public List<PumaServerEntity> getPumaServers() {
		return pumaServers;
	}

	public Date getUpdateTime() {
		return UpdateTime;
	}

	public void setUpdateTime(Date updateTime) {
		UpdateTime = updateTime;
	}

	public ActionController getActionController() {
		return actionController;
	}

	public void setActionController(ActionController actionController) {
		this.actionController = actionController;
	}
}
