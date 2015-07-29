package com.dianping.puma.storage;

import com.dianping.puma.core.LifeCycle;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.storage.bucket.BucketManager;
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.storage.exception.StorageLifeCycleException;
import com.dianping.puma.storage.index.IndexKeyImpl;
import com.dianping.puma.storage.index.IndexManager;
import com.dianping.puma.storage.index.IndexValueImpl;

public interface EventStorage extends LifeCycle<StorageLifeCycleException> {

	public void store(ChangedEvent event) throws StorageException;
	
	public BucketManager getBucketManager();
	
	public IndexManager<IndexKeyImpl, IndexValueImpl> getDataIndex();
	
	public EventCodec getEventCodec();

}
