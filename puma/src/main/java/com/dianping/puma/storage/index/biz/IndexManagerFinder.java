package com.dianping.puma.storage.index.biz;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.index.manage.ReadIndexManager;
import com.dianping.puma.storage.index.manage.WriteIndexManager;

public interface IndexManagerFinder extends LifeCycle {

	ReadIndexManager<L1IndexKey, L1IndexValue> findL1ReadIndexManager();

	ReadIndexManager<L2IndexKey, L2IndexValue> findL2ReadIndexManager(L1IndexValue l1IndexValue);

	WriteIndexManager<L1IndexKey, L1IndexValue> findL1WriteIndexManager();

	WriteIndexManager<L2IndexKey, L2IndexValue> findNextL2WriteIndexManager();
}
