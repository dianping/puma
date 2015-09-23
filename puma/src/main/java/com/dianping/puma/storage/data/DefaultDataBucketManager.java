package com.dianping.puma.storage.data;

import com.dianping.puma.common.AbstractLifeCycle;
import com.dianping.puma.storage.Sequence;

public class DefaultDataBucketManager extends AbstractLifeCycle implements DataBucketManager {

	private String baseDir;

	private String database;

	public DefaultDataBucketManager(String baseDir, String database) {
		this.baseDir = baseDir;
		this.database = database;
	}

	@Override
	protected void doStart() {

	}

	@Override
	protected void doStop() {

	}

	@Override
	public DataBucket findReadDataBucket(Sequence sequence) {
		return null;
	}

	@Override
	public DataBucket findNextReadDataBucket(Sequence sequence) {
		return null;
	}
}
