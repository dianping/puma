package com.dianping.puma.storage.data;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.Sequence;

import java.io.IOException;

public interface ReadDataManager extends LifeCycle {

	public void open(Sequence sequence) throws IOException;

	public byte[] next() throws IOException;
}
