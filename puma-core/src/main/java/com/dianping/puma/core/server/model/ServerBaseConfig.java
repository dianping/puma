package com.dianping.puma.core.server.model;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

public class ServerBaseConfig {

	@Id
	private ObjectId id;

	@Indexed(value = IndexDirection.ASC, name = "host", unique = true, dropDups = true)
	private String host;

	@Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
	private String name;

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "PumaServerBaseConfig [id=" + id + ", name=" + name + ", host="
				+ host + "]";
	}
}
