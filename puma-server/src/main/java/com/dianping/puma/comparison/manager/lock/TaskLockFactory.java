package com.dianping.puma.comparison.manager.lock;

public class TaskLockFactory {

	public static TaskLock getDatabaseTaskLock(int id) {
		return new DatabaseTaskLock(id);
	}
}
