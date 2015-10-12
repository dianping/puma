package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface WriteDataBucket extends LifeCycle {

	public void append(byte[] data) throws IOException;

	public void flush() throws IOException;

	public boolean hasRemainingForWrite() throws IOException;
}
