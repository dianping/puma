package com.dianping.puma.api.router;

import java.util.List;
import java.util.Map;

public class RemotePumaServerRouter implements PumaServerRouter {

	@Override
	public Map<String, Float> route(String database, List<String> tables) {
		return null;
	}

	@Override
	public void addListener(PumaServerListener listener) {
	}

	private Map<String, Float> request() {
		return null;
	}
}
