package com.dianping.puma.core.replicate.model.task;

import com.dianping.puma.core.replicate.model.config.DBInstanceConfig;
import com.dianping.puma.core.replicate.model.config.ServerConfig;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import org.bson.types.ObjectId;

@Entity
public class ReplicationTask {

	@Id
	private ObjectId id;

	private DBInstanceConfig dbInstanceConfig;

	private ServerConfig serverConfig;

	private BinlogInfo binlogInfo;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
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
