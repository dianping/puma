package com.dianping.puma.storage;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;

public interface EventStorage extends LifeCycle<StorageLifeCycleException> {
	public EventChannel getChannel(long seq, long serverId, String binlogFile,
			String binlogPos) throws StorageException;

	public void store(ChangedEvent event) throws StorageException;

}
