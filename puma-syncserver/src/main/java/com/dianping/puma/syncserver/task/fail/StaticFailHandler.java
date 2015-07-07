package com.dianping.puma.syncserver.task.fail;

public class StaticFailHandler implements FailHandler {

	@Override
	public FailPattern handle(Exception e) {
		return null;
	}
}
