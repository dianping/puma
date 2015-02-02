package com.dianping.puma.core.server.model;

import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

@Entity
public class ServerTask extends BaseEntity{
	

	@Indexed(value = IndexDirection.ASC,name="hostIndex")
	private String serverName;
	
	@Indexed(value = IndexDirection.ASC, name = "taskIdIndex", unique = true, dropDups = true)
	private long taskId;
	
	@Indexed(value = IndexDirection.ASC, name = "taskNameIndex", unique = true, dropDups = true)
	private String taskName;

	private String dbHost;

	private int dbPort;

	private String dbUser;

	private String dbPassword;

	private long dbServerId;
	
	private String defaultBinlogFileName;
	
	private long defaultBinlogPosition;
	
	private String metaDBHost;
	
	private int metaDBPort;
	
	private String metaDBUser;
	
	private String metaDBPassword;
	
	private String dispatcherName;
	
	private List<FileSenderConfig> fileSenders;
	
	private ServerTaskActionStatus statusAction;

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}

	public String getDbHost() {
		return dbHost;
	}

	public void setDbPort(int dbPort) {
		this.dbPort = dbPort;
	}

	public int getDbPort() {
		return dbPort;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbServerId(long dbServerId) {
		this.dbServerId = dbServerId;
	}

	public long getDbServerId() {
		return dbServerId;
	}

	public void setDefaultBinlogFileName(String defaultBinlogFileName) {
		this.defaultBinlogFileName = defaultBinlogFileName;
	}

	public String getDefaultBinlogFileName() {
		return defaultBinlogFileName;
	}

	public void setDefaultBinlogPosition(long defaultBinlogPosition) {
		this.defaultBinlogPosition = defaultBinlogPosition;
	}

	public long getDefaultBinlogPosition() {
		return defaultBinlogPosition;
	}

	public void setMetaDBHost(String metaDBHost) {
		this.metaDBHost = metaDBHost;
	}

	public String getMetaDBHost() {
		return metaDBHost;
	}

	public void setMetaDBPort(int metaDBPort) {
		this.metaDBPort = metaDBPort;
	}

	public int getMetaDBPort() {
		return metaDBPort;
	}

	public void setMetaDBUser(String metaDBUser) {
		this.metaDBUser = metaDBUser;
	}

	public String getMetaDBUser() {
		return metaDBUser;
	}

	public void setMetaDBPassword(String metaDBPassword) {
		this.metaDBPassword = metaDBPassword;
	}

	public String getMetaDBPassword() {
		return metaDBPassword;
	}

	public void setDispatcherName(String dispatcherName) {
		this.dispatcherName = dispatcherName;
	}

	public String getDispatcherName() {
		return dispatcherName;
	}

	public void setFileSenders(List<FileSenderConfig> fileSenders) {
		this.fileSenders = fileSenders;
	}

	public List<FileSenderConfig> getFileSenders() {
		return fileSenders;
	}

	public void setStatusAction(ServerTaskActionStatus statusAction) {
		this.statusAction = statusAction;
	}

	public ServerTaskActionStatus getStatusAction() {
		return statusAction;
	}
	
}
