package com.dianping.puma.core.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributedLockUtils {

	private static final Logger logger = LoggerFactory.getLogger(DistributedLockUtils.class);

	public static void unlockQuietly(DistributedLock lock)	{
		try {
			lock.unlock();
		} catch (Throwable t) {
			logger.error("failed to unlock, error should not be thrown.", t);
		}
	}

	public static void unlockNotifyQuietly(DistributedLock lock) {
		try {
			lock.unlockNotify();
		} catch (Throwable t) {
			logger.error("failed to unlock notify, error should not be thrown.");
		}
	}

}
