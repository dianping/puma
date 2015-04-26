package com.dianping.puma.core.entity;

import java.util.Map;

import com.dianping.puma.core.model.AcceptedTables;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.SchemaTableSet;

public class PumaTask extends BaseEntity {

	private String srcDBInstanceName;

	private String pumaServerName;

	private BinlogInfo binlogInfo;

	private int preservedDay;

	private SchemaTableSet schemaTableSet;

	@Deprecated
	private Map<String,AcceptedTables> acceptedDataInfos;

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

	public SchemaTableSet getSchemaTableSet() {
		return schemaTableSet;
	}

	public void setSchemaTableSet(SchemaTableSet schemaTableSet) {
		this.schemaTableSet = schemaTableSet;
	}

	@Deprecated
	public void setAcceptedDataInfos(Map<String,AcceptedTables> acceptedDataInfos) {
		this.acceptedDataInfos = acceptedDataInfos;
	}

	@Deprecated
	public Map<String,AcceptedTables> getAcceptedDataInfos() {
		return acceptedDataInfos;
	}
}
