package com.dianping.puma.syncserver.load.model;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.model.BatchRow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class BatchRowTest {

	BatchRow batchRow;

	@Before
	public void before() {
		batchRow = new BatchRow();
	}

	@Test
	public void testAddRow0() {
		// row0: puma.test | UPDATE | (1, "Peter")->(1, "Linda").
		RowChangedEvent row0 = new RowChangedEvent();
		row0.setDatabase("puma");
		row0.setTable("test");
		row0.setDmlType(DMLType.UPDATE);
		Map<String, ColumnInfo> columnInfoMap0 = new HashMap<String, ColumnInfo>();
		columnInfoMap0.put("id", new ColumnInfo(true, 1, 1));
		columnInfoMap0.put("name", new ColumnInfo(false, "Peter", "Linda"));
		row0.setColumns(columnInfoMap0);
		Assert.assertTrue(batchRow.addRow(row0));

		// row1: puma.test | UPDATE | (2, "Kevin")->(2, "Jason").
		RowChangedEvent row1 = new RowChangedEvent();
		row1.setDatabase("puma");
		row1.setTable("test");
		row1.setDmlType(DMLType.UPDATE);
		Map<String, ColumnInfo> columnInfoMap1 = new HashMap<String, ColumnInfo>();
		columnInfoMap1.put("id", new ColumnInfo(true, 2, 2));
		columnInfoMap1.put("name", new ColumnInfo(false, "Kevin", "Jason"));
		row1.setColumns(columnInfoMap1);
		Assert.assertTrue(batchRow.addRow(row1));

		Object[][] result = batchRow.getParams();
		Object[][] expected = new Object[2][3];
		expected[0] = new Object[]{1, "Linda", 1};
		expected[1] = new Object[]{2, "Jason", 2};
		Assert.assertArrayEquals(expected, result);

		// row2: puma.test | INSERT | (2, "Jason").
		RowChangedEvent row2 = new RowChangedEvent();
		row2.setDatabase("puma");
		row2.setTable("test");
		row2.setDmlType(DMLType.INSERT);
		Map<String, ColumnInfo> columnInfoMap2 = new HashMap<String, ColumnInfo>();
		columnInfoMap2.put("id", new ColumnInfo(true, null, 2));
		columnInfoMap2.put("name", new ColumnInfo(false, null, "Jason"));
		row2.setColumns(columnInfoMap2);
		Assert.assertFalse(batchRow.addRow(row2));

		// row3: puma.test | UPDATE | (2, "Jason")->(2, "Jordan").
		RowChangedEvent row3 = new RowChangedEvent();
		row3.setDatabase("puma");
		row3.setTable("test");
		row3.setDmlType(DMLType.UPDATE);
		Map<String, ColumnInfo> columnInfoMap3 = new HashMap<String, ColumnInfo>();
		columnInfoMap3.put("id", new ColumnInfo(true, 2, 2));
		columnInfoMap3.put("name", new ColumnInfo(false, "Jason", "Jordan"));
		row3.setColumns(columnInfoMap3);
		Assert.assertFalse(batchRow.addRow(row3));

		// row4: puma.hello | UPDATE | (3, "Jason")->(3, "Jordan").
		RowChangedEvent row4 = new RowChangedEvent();
		row4.setDatabase("puma");
		row4.setTable("hello");
		row4.setDmlType(DMLType.UPDATE);
		Map<String, ColumnInfo> columnInfoMap4 = new HashMap<String, ColumnInfo>();
		columnInfoMap4.put("id", new ColumnInfo(true, 3, 3));
		columnInfoMap4.put("name", new ColumnInfo(false, "Jason", "Jordan"));
		row4.setColumns(columnInfoMap4);
		Assert.assertFalse(batchRow.addRow(row4));

		// ddl0: puma.test | ALTER TABLE
		DdlEvent ddl0 = new DdlEvent();
		ddl0.setDatabase("puma");
		ddl0.setTable("test");
		ddl0.setDDLType(DDLType.ALTER_TABLE);
		Assert.assertFalse(batchRow.addRow(ddl0));
	}

	@Test
	public void testAddRow1() {
		// ddl0: puma.test | ALTER TABLE
		DdlEvent ddl0 = new DdlEvent();
		ddl0.setDatabase("puma");
		ddl0.setTable("test");
		ddl0.setDDLType(DDLType.ALTER_TABLE);
		Assert.assertTrue(batchRow.addRow(ddl0));

		// row0: puma.test | UPDATE | (1, "Peter")->(1, "Linda").
		RowChangedEvent row0 = new RowChangedEvent();
		row0.setDatabase("puma");
		row0.setTable("test");
		row0.setDmlType(DMLType.UPDATE);
		Map<String, ColumnInfo> columnInfoMap0 = new HashMap<String, ColumnInfo>();
		columnInfoMap0.put("id", new ColumnInfo(true, 1, 1));
		columnInfoMap0.put("name", new ColumnInfo(false, "Peter", "Linda"));
		row0.setColumns(columnInfoMap0);
		Assert.assertFalse(batchRow.addRow(row0));
	}
}
