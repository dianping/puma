package com.dianping.puma.syncserver.manager.fail;

public class FailHandler {

	public static final FailHandler INSTANCE = new FailHandler();

	public FailPattern handle(String name, Exception e) {
		return null;
	}

	public void register(String name, Exception e, FailPattern failPattern) {

	}

	public void unregister(String name) {

	}
}
