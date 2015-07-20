package com.dianping.puma.api.router;

import java.util.List;
import java.util.Map;

public interface PumaServerRouter {

	public Map<String, Float> route(String database, List<String> tables);

	public void addListener(PumaServerListener listener);
}
