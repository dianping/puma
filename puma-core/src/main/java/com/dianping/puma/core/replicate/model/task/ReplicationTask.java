package com.dianping.puma.core.replicate.model.task;

import com.dianping.puma.core.replicate.model.config.DBInstanceHost;
import com.dianping.puma.core.replicate.model.config.FileSenderConfig;
import com.dianping.puma.core.replicate.model.BaseEntity;
import com.dianping.puma.core.model.BinlogInfo;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

import java.util.List;

@Entity
public class ReplicationTask extends BaseEntity {

	@Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
	private long taskId;

	@Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
	private String taskName;

	private String dbInstanceName;

	private DBInstanceHost dbInstanceHost;

	private DBInstanceHost dbInstanceMetaHost;

	private String replicationServerName;

	private BinlogInfo binlogInfo;

	private List<FileSenderConfig> fileSenderConfigs;

	private String dispatchName;

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getDbInstanceName() {
		return dbInstanceName;
	}

	public void setDbInstanceName(String dbInstanceName) {
		this.dbInstanceName = dbInstanceName;
	}

	public DBInstanceHost getDbInstanceHost() {
		return dbInstanceHost;
	}

	public void setDbInstanceHost(DBInstanceHost dbInstanceHost) {
		this.dbInstanceHost = dbInstanceHost;
	}

	public DBInstanceHost getDbInstanceMetaHost() {
		return dbInstanceMetaHost;
	}

	public void setDbInstanceMetaHost(DBInstanceHost dbInstanceMetaHost) {
		this.dbInstanceMetaHost = dbInstanceMetaHost;
	}

	public String getReplicationServerName() {
		return replicationServerName;
	}

	public void setReplicationServerName(String replicationServerName) {
		this.replicationServerName = replicationServerName;
	}

	public List<FileSenderConfig> getFileSenderConfigs() {
		return fileSenderConfigs;
	}

	public void setFileSenderConfigs(List<FileSenderConfig> fileSenderConfigs) {
		this.fileSenderConfigs = fileSenderConfigs;
	}

	public String getDispatchName() {
		return dispatchName;
	}

	public void setDispatchName(String dispatchName) {
		this.dispatchName = dispatchName;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}


}
