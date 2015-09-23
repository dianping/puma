package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.Sequence;

public interface DataBucketManager extends LifeCycle {

	public DataBucket findReadDataBucket(Sequence sequence);

	public DataBucket findNextReadDataBucket(Sequence sequence);
}
