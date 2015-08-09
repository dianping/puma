package com.dianping.puma.admin.ds;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

	private String name;

	private List<String> databases = new ArrayList<String>();

	private List<Single> singles = new ArrayList<Single>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getDatabases() {
		return databases;
	}

	public void addJdbcRef(String jdbcRef) {
		databases.add(jdbcRef);
	}

	public void setDatabases(List<String> databases) {
		this.databases = databases;
	}

	public List<Single> getSingles() {
		return singles;
	}

	public void addSingle(Single single) {
		singles.add(single);
	}

	public void setSingles(List<Single> singles) {
		this.singles = singles;
	}
}
