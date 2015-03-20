package com.dianping.puma.core.entity;

public class PumaClientInfoEntity extends BaseEntity {

	private String target;

	private String name;

	private String ip;

	private long seq;

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTarget() {
		return target;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public long getSeq() {
		return seq;
	}

}
