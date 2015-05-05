package com.dianping.puma.syncserver.load;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.model.BatchRows;
import org.junit.Before;
import org.junit.Test;

public class LoadMergeTest {

	BatchRows batchRows = new BatchRows();

	@Before
	public void before() {
		batchRows.clear();

		RowChangedEvent row0 = new RowChangedEvent();
		row0.setDMLType(DMLType.INSERT);
		row0.setDatabase("puma");
		row0.setTable("test");
	}

	@Test
	public void mergeInsertTest() {

	}
}
