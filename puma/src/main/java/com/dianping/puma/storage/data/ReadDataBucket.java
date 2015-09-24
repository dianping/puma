package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface ReadDataBucket extends LifeCycle {

	public long offset();

	public byte[] next() throws IOException;

	public void skip(long offset) throws IOException;
}
