package com.dianping.puma.api;

public interface PumaServerRouter {

	public String next();

	public boolean exist(String server);
}
