package com.dianping.puma.core.entity;

import com.dianping.puma.core.constant.SyncType;

public abstract class BaseSyncTask extends BaseEntity {

	private SyncType syncType;

	private String pumaTaskName;

	private String dstDBInstanceName;

	private String syncServerName;

	public SyncType getSyncType() {
		return syncType;
	}

	public void setSyncType(SyncType syncType) {
		this.syncType = syncType;
	}

	public String getPumaTaskName() {
		return pumaTaskName;
	}

	public void setPumaTaskName(String pumaTaskName) {
		this.pumaTaskName = pumaTaskName;
	}

	public String getDstDBInstanceName() {
		return dstDBInstanceName;
	}

	public void setDstDBInstanceName(String dstDBInstanceName) {
		this.dstDBInstanceName = dstDBInstanceName;
	}

	public String getSyncServerName() {
		return syncServerName;
	}

	public void setSyncServerName(String syncServerName) {
		this.syncServerName = syncServerName;
	}
}
