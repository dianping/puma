package com.dianping.puma.biz.entity;

import java.util.List;
import java.util.Map;

import com.dianping.puma.core.model.AcceptedTables;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.TableSet;

public class PumaTask extends BaseEntity {

	private static final long serialVersionUID = 1853488084226378816L;

	private String srcDBInstanceName;

	private String pumaServerName;
	
	private List<String> pumaServerNames;

	private BinlogInfo binlogInfo;

	private int preservedDay;

	private TableSet tableSet;

	@Deprecated
	private Map<String, AcceptedTables> acceptedDataInfos;

	public String getSrcDBInstanceName() {
		return srcDBInstanceName;
	}

	public void setSrcDBInstanceName(String srcDBInstanceName) {
		this.srcDBInstanceName = srcDBInstanceName;
	}

	public String getPumaServerName() {
		return pumaServerName;
	}

	public void setPumaServerName(String pumaServerName) {
		this.pumaServerName = pumaServerName;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public int getPreservedDay() {
		return preservedDay;
	}

	public void setPreservedDay(int preservedDay) {
		this.preservedDay = preservedDay;
	}

	public TableSet getTableSet() {
		return tableSet;
	}

	public void setTableSet(TableSet tableSet) {
		this.tableSet = tableSet;
	}

	@Deprecated
	public void setAcceptedDataInfos(Map<String, AcceptedTables> acceptedDataInfos) {
		this.acceptedDataInfos = acceptedDataInfos;
	}

	@Deprecated
	public Map<String, AcceptedTables> getAcceptedDataInfos() {
		return acceptedDataInfos;
	}

	public List<String> getPumaServerNames() {
		return pumaServerNames;
	}

	public void setPumaServerNames(List<String> pumaServerNames) {
		this.pumaServerNames = pumaServerNames;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null || !(o instanceof PumaTask)) {
			return false;
		}
		PumaTask pumaTask = (PumaTask) o;
		if (this.getBinlogInfo().equals(pumaTask.getBinlogInfo())
				&& (this.getPreservedDay() == pumaTask.getPreservedDay())
				&& this.getTableSet().equals(pumaTask.getTableSet()) && this.getName().equals(pumaTask.getName())
				&& this.getPumaServerName().equals(pumaTask.getPumaServerName())
				&& this.getSrcDBInstanceName().equals(pumaTask.getSrcDBInstanceName())) {
			return true;
		}
		return false;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.getBinlogInfo().hashCode();
		result = prime * result + this.getPreservedDay();
		result = prime * result + this.getTableSet().hashCode();
		result = prime * result + this.getName().hashCode();
		result = prime * result + this.getPumaServerName().hashCode();
		result = prime * result + this.getSrcDBInstanceName().hashCode();
		return result;
	}
}
