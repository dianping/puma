package com.dianping.puma.api;

public interface PumaServerRouter {

	String next();

	boolean exist(String server);
}
