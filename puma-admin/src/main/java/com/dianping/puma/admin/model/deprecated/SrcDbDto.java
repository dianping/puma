package com.dianping.puma.admin.model.deprecated;

import com.dianping.puma.biz.entity.SrcDbEntity;

import java.util.ArrayList;
import java.util.List;

public class SrcDbDto {

	private String mhaRef;

	private List<SrcDbEntity> srcDbEntities = new ArrayList<SrcDbEntity>();

	private String username;

	private String password;

	public String getMhaRef() {
		return mhaRef;
	}

	public void setMhaRef(String mhaRef) {
		this.mhaRef = mhaRef;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<SrcDbEntity> getSrcDbEntities() {
		return srcDbEntities;
	}

	public void setSrcDbEntities(List<SrcDbEntity> srcDbEntities) {
		this.srcDbEntities = srcDbEntities;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
