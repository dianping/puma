package com.dianping.puma.api.router;

import java.util.Map;

public interface PumaServerRouterListener {

	public void onChange(Map<String, Double> pumaServers);
}
