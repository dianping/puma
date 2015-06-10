package com.dianping.puma.api.manager;

import java.util.ArrayList;
import java.util.List;

public class HostManager {

	private String name;

	private List<String> hosts = new ArrayList<String>();

	public HostManager() {
	}

	public void init() {

	}

	public String next() {
		return new String();
	}

	public void success() {

	}

	public void failure() {

	}

	public void setName(String name) {
		this.name = name;
	}
}
