package com.dianping.puma.core.monitor;

public class PumaTaskEvent extends Event {

	String pumaServerName;

	public String getPumaServerName() {
		return pumaServerName;
	}

	public void setPumaServerName(String pumaServerName) {
		this.pumaServerName = pumaServerName;
	}
}
