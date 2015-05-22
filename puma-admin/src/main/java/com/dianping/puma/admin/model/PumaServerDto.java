package com.dianping.puma.admin.model;

public class PumaServerDto {
	
	private String name;
	
	private String host;

	private int port;

	public PumaServerDto(){
		
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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
