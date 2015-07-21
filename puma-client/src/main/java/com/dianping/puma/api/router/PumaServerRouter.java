package com.dianping.puma.api.router;

import java.util.List;
import java.util.Map;

public interface PumaServerRouter {

	public void init(String database, List<String> tables);

	public Map<String, Double> route();
}
