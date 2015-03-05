package com.dianping.puma.core.monitor;

public class PumaTaskEvent extends Event {

	String pumaServerId;

	public String getPumaServerId() {
		return pumaServerId;
	}

	public void setPumaServerId(String pumaServerId) {
		this.pumaServerId = pumaServerId;
	}
}
