package com.dianping.puma.biz.entity;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.TableSet;

import java.util.Date;
import java.util.List;

public class PumaTaskEntity {

	private int id;

	private String name;

	private BinlogInfo binlogInfo;

	private int preservedDay;

	private TableSet tableSet;

	private SrcDbEntity perferSrcDb;

	private List<SrcDbEntity> backUpSrcDbs;

	private List<PumaServerEntity> pumaServers;

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

	public SrcDbEntity getPerferSrcDb() {
		return perferSrcDb;
	}

	public void setPerferSrcDb(SrcDbEntity perferSrcDb) {
		this.perferSrcDb = perferSrcDb;
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
