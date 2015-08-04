package com.dianping.puma.api;

import java.util.List;

public interface PumaServerRouter {

	public void init(String database, List<String> tables);

	public String next();
}
