package com.dianping.puma.core.replicate.model.config;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

import java.util.List;

@Entity
public class DBInstanceConfig {

	@Id
	private ObjectId id;

	@Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
	private String name;

	private List<DBInstanceHost> dbInstanceHosts;

	private List<DBInstanceHost> dbInstanceMetaHosts;

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

	public List<DBInstanceHost> getDbInstanceHosts() {
		return dbInstanceHosts;
	}

	public void setDbInstanceHosts(List<DBInstanceHost> dbInstanceHosts) {
		this.dbInstanceHosts = dbInstanceHosts;
	}

	public List<DBInstanceHost> getDbInstanceMetaHosts() {
		return dbInstanceMetaHosts;
	}

	public void setDbInstanceMetaHosts(List<DBInstanceHost> dbInstanceMetaHosts) {
		this.dbInstanceMetaHosts = dbInstanceMetaHosts;
	}
}
