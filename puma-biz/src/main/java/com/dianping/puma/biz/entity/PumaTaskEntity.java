package com.dianping.puma.biz.entity;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.TableSet;

import java.util.Date;
import java.util.List;

public class PumaTaskEntity {

	private int id;

	private String name;

	/** parsed binlog preserved days. */
	private int preservedDay;

	/** source db jdbcRef. */
	private String jdbcRef;

	/** start binlog info, should be valid to the source db cluster. */
	private BinlogInfo startBinlogInfo;

	private Date UpdateTime;

	/** relations with `PumaTaskTarget`. */
	private TableSet tableSet;

	/** relations with `PumaServer` by `PumaTaskServer`. */
	private List<PumaServerEntity> pumaServers;

	/** relations with `SrcDb`. */
	private SrcDbEntity preferredSrcDb;

	/** relations with `SrcDb`. */
	private List<SrcDbEntity> backUpSrcDbs;

	/** puma task instance, stopped or started on a specific server. */
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

	public BinlogInfo getStartBinlogInfo() {
		return startBinlogInfo;
	}

	public void setStartBinlogInfo(BinlogInfo startBinlogInfo) {
		this.startBinlogInfo = startBinlogInfo;
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

	public SrcDbEntity getPreferredSrcDb() {
		return preferredSrcDb;
	}

	public void setPreferredSrcDb(SrcDbEntity preferredSrcDb) {
		this.preferredSrcDb = preferredSrcDb;
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

	public String getJdbcRef() {
		return jdbcRef;
	}

	public void setJdbcRef(String jdbcRef) {
		this.jdbcRef = jdbcRef;
	}
}
