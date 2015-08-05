package com.dianping.puma.api;

import java.util.List;

public interface PumaServerRouter {

	public String next(String database, List<String> tables);
}
