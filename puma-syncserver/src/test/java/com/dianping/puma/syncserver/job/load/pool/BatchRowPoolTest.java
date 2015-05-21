package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.row.BatchRow;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class BatchRowPoolTest {

	BatchRowPool batchRowPool;

	@Before
	public void before() {
		batchRowPool = new BatchRowPool();
		batchRowPool.start();
	}

	@After
	public void after() {
		batchRowPool.stop();
	}

	@Test
	public void testPutAndTake0() throws InterruptedException {
		BatchRow batchRow = new BatchRow();

		// row0: puma.test | UPDATE | (1, "Peter")->(1, "Linda").
		RowChangedEvent row0 = new RowChangedEvent();
		row0.setDatabase("puma");
		row0.setTable("test");
		row0.setDmlType(DMLType.UPDATE);
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap0 = new HashMap<String, RowChangedEvent.ColumnInfo>();
		columnInfoMap0.put("id", new RowChangedEvent.ColumnInfo(true, 1, 1));
		columnInfoMap0.put("name", new RowChangedEvent.ColumnInfo(false, "Peter", "Linda"));
		row0.setColumns(columnInfoMap0);
		Assert.assertTrue(batchRow.addRow(row0));

		// row1: puma.test | UPDATE | (2, "Kevin")->(2, "Jason").
		RowChangedEvent row1 = new RowChangedEvent();
		row1.setDatabase("puma");
		row1.setTable("test");
		row1.setDmlType(DMLType.UPDATE);
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap1 = new HashMap<String, RowChangedEvent.ColumnInfo>();
		columnInfoMap1.put("id", new RowChangedEvent.ColumnInfo(true, 2, 2));
		columnInfoMap1.put("name", new RowChangedEvent.ColumnInfo(false, "Kevin", "Jason"));
		row1.setColumns(columnInfoMap1);
		Assert.assertTrue(batchRow.addRow(row1));

		batchRowPool.put(row0);
		batchRowPool.put(row1);
		BatchRow result = batchRowPool.take();
		Assert.assertTrue(EqualsBuilder.reflectionEquals(batchRow.getParams(), result.getParams()));
	}

	@Test
	public void testPutAndTake1() throws InterruptedException {
		// row0: puma.test | UPDATE | (1, "Peter")->(1, "Linda").
		RowChangedEvent row0 = new RowChangedEvent();
		row0.setDatabase("puma");
		row0.setTable("test");
		row0.setDmlType(DMLType.UPDATE);
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap0 = new HashMap<String, RowChangedEvent.ColumnInfo>();
		columnInfoMap0.put("id", new RowChangedEvent.ColumnInfo(true, 1, 1));
		columnInfoMap0.put("name", new RowChangedEvent.ColumnInfo(false, "Peter", "Linda"));
		row0.setColumns(columnInfoMap0);

		// row1: puma.test | UPDATE | (1, "Linda")->(1, "Russel").
		RowChangedEvent row1 = new RowChangedEvent();
		row1.setDatabase("puma");
		row1.setTable("test");
		row1.setDmlType(DMLType.UPDATE);
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap1 = new HashMap<String, RowChangedEvent.ColumnInfo>();
		columnInfoMap1.put("id", new RowChangedEvent.ColumnInfo(true, 1, 1));
		columnInfoMap1.put("name", new RowChangedEvent.ColumnInfo(false, "Linda", "Russel"));
		row1.setColumns(columnInfoMap1);

		batchRowPool.put(row0);
		batchRowPool.put(row1);
		BatchRow result0 = batchRowPool.take();
		BatchRow expected0 = new BatchRow(row0);
		BatchRow result1 = batchRowPool.take();
		BatchRow expected1 = new BatchRow(row1);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected0.getParams(), result0.getParams()));
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected1.getParams(), result1.getParams()));
	}
}
