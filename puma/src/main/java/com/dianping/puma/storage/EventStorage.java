package com.dianping.puma.storage;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.bucket.BucketManager;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;
import com.dianping.puma.storage.oldindex.IndexKeyImpl;
import com.dianping.puma.storage.oldindex.IndexValueImpl;
import com.dianping.puma.storage.oldindex.WriteIndexManager;

public interface EventStorage extends LifeCycle<StorageLifeCycleException> {

	public void store(ChangedEvent event) throws StorageException;

	public void flush();

	public BucketManager getBucketManager();

	public WriteIndexManager<IndexKeyImpl, IndexValueImpl> getWriteIndexManager();

	public EventCodec getEventCodec();

}
