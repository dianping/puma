package com.dianping.puma.core.replicate.model.config;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;
import org.bson.types.ObjectId;

@Entity
public class ServerConfig {

	@Id
	private ObjectId id;

	@Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
	private String name;

	// host:port
	private String host;

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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
}
