package com.dianping.puma.core.event;

public class ServerErrorEvent extends Event {

	private Exception e;

	public ServerErrorEvent(Exception e) {
		this.e = e;
	}
}
