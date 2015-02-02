package com.dianping.puma.core.replicate.model.config;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

@Entity
public class DBInstanceConfig {

	@Id
	private ObjectId id;

	@Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
	private String name;

	private DBInstanceHost dbInstanceHost;

	private DBInstanceHost dbInstanceMetaHost;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
}
