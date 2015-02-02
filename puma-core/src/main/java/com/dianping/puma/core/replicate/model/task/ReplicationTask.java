package com.dianping.puma.core.replicate.model.task;

import com.dianping.puma.core.replicate.model.config.DBInstanceConfig;
import com.dianping.puma.core.replicate.model.config.FileSenderConfig;
import com.dianping.puma.core.replicate.model.config.ServerConfig;
import com.dianping.puma.core.replicate.model.BaseEntity;
import com.dianping.puma.core.replicate.model.BinlogInfo;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;
import org.bson.types.ObjectId;

import java.util.List;

@Entity
public class ReplicationTask extends BaseEntity {

	@Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
	private long taskId;

	@Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
	private String taskName;

	private DBInstanceConfig dbInstanceConfig;

	private ServerConfig serverConfig;

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

	public DBInstanceConfig getDbInstanceConfig() {
		return dbInstanceConfig;
	}

	public void setDbInstanceConfig(DBInstanceConfig dbInstanceConfig) {
		this.dbInstanceConfig = dbInstanceConfig;
	}

	public ServerConfig getServerConfig() {
		return serverConfig;
	}

	public void setServerConfig(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}

	public BinlogInfo getBinlogInfo() {
		return binlogInfo;
	}

	public void setBinlogInfo(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
	}


}
