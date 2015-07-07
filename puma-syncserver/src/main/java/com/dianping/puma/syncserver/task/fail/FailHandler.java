package com.dianping.puma.syncserver.task.fail;

public interface FailHandler {

	FailPattern handle(Exception e);
}
