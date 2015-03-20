package com.dianping.puma.core.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = 8121775127353895000L;

	private String id;

	private String name;

	private Date gmtCreate;

	private Date gmtModify;

	private Long version;

	public BaseEntity() {
		this.id = UUID.randomUUID().toString();
		this.upgrade();
	}

	public void upgrade() {
		Date now = new Date();

		if (this.gmtCreate == null) {
			this.gmtCreate = now;
		}

		this.gmtModify = now;

		if (this.version == null) {
			this.version = 1L;
		}
		++this.version;
	}

	public String getId() {
		return id;
	}

	public String getName() { return name; }

	public void setName(String name) {
		this.name = name;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public Date getGmtModify() {
		return gmtModify;
	}

	public Long getVersion() {
		return version;
	}
}
