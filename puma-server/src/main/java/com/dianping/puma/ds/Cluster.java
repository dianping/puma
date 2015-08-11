package com.dianping.puma.ds;

import java.util.HashSet;
import java.util.Set;

public class Cluster {

	private String name;

	private Set<String> databases = new HashSet<String>();

	private Set<Single> singles = new HashSet<Single>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getDatabases() {
		return databases;
	}

	public void addDatabase(String database) {
		databases.add(database);
	}

	public void setDatabases(Set<String> databases) {
		this.databases = databases;
	}

	public Set<Single> getSingles() {
		return singles;
	}

	public void addSingle(Single single) {
		singles.add(single);
	}

	public void setSingles(Set<Single> singles) {
		this.singles = singles;
	}
}
