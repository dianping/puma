package com.dianping.puma.core.replicate.model.config;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;
import org.bson.types.ObjectId;

import java.util.List;

@Entity
public class ServerConfig {

	@Id
	private ObjectId id;

	@Indexed(value = IndexDirection.ASC, name = "name", unique = true, dropDups = true)
	private String name;

	private List<String> hosts;

	private List<String> ports;

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

	public List<String> getHosts() {
		return hosts;
	}

	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}

	public List<String> getPorts() {
		return ports;
	}

	public void setPorts(List<String> ports) {
		this.ports = ports;
	}
}
