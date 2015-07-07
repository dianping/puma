package com.dianping.puma.biz.entity;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.model.BinlogInfo;

import java.util.List;
import java.util.Map;

public abstract class BaseSyncTask extends BaseEntity {

	private SyncType syncType;

	private String pumaTaskName;

	private String dstDBInstanceName;

	private String syncServerName;
	
	private List<String> syncServerNames;

	private BinlogInfo binlogInfo;

	private ActionController controller;

	private Map<Integer, String> errorCodeHandlerNameMap;

	private String defaultHandler;

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

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}

	public ActionController getController() {
		return controller;
	}

	public void setController(ActionController controller) {
		this.controller = controller;
	}

	public Map<Integer, String> getErrorCodeHandlerNameMap() {
		return errorCodeHandlerNameMap;
	}

	public void setErrorCodeHandlerNameMap(Map<Integer, String> errorCodeHandlerNameMap) {
		this.errorCodeHandlerNameMap = errorCodeHandlerNameMap;
	}

	public String getDefaultHandler() {
		return defaultHandler;
	}

	public void setDefaultHandler(String defaultHandler) {
		this.defaultHandler = defaultHandler;
	}

	public List<String> getSyncServerNames() {
		return syncServerNames;
	}

	public void setSyncServerNames(List<String> syncServerNames) {
		this.syncServerNames = syncServerNames;
	}
}
