package com.dianping.puma.storage.index;

import com.dianping.puma.common.LifeCycle;

import java.io.IOException;

public interface IndexManagerFinder extends LifeCycle {

	ReadIndexManager<L1IndexKey, L1IndexValue> findL1ReadIndexManager() throws IOException;

	ReadIndexManager<L2IndexKey, L2IndexValue> findL2ReadIndexManager(L1IndexValue l1IndexValue) throws IOException;

	WriteIndexManager<L1IndexKey, L1IndexValue> findL1WriteIndexManager() throws IOException;

	WriteIndexManager<L2IndexKey, L2IndexValue> findNextL2WriteIndexManager() throws IOException;
}
