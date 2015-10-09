package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.Sequence;

import java.io.IOException;

public interface ReadDataBucket extends LifeCycle {

	public Sequence sequence();

	public byte[] next() throws IOException;

	public void skip(long offset) throws IOException;
}
