package com.dianping.puma.syncserver.load.model;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
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
	public void addRowTest() {
		RowChangedEvent row0 = new RowChangedEvent();
		row0.setDatabase("puma");
		row0.setTable("test");
		row0.setDmlType(DMLType.UPDATE);
		Map<String, ColumnInfo> columnInfoMap0 = new HashMap<String, ColumnInfo>();
		columnInfoMap0.put("id", new ColumnInfo(true, 1, 1));
		columnInfoMap0.put("name", new ColumnInfo(false, "Peter", "Linda"));
		row0.setColumns(columnInfoMap0);
		Assert.assertTrue(batchRow.addRow(row0));

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

		//System.out.println(batchRow.getSql());

		RowChangedEvent row2 = new RowChangedEvent();
		row2.setDatabase("puma");
		row2.setTable("test");
		row2.setDmlType(DMLType.INSERT);
		Map<String, ColumnInfo> columnInfoMap2 = new HashMap<String, ColumnInfo>();
		columnInfoMap2.put("id", new ColumnInfo(true, 2, 2));
		columnInfoMap2.put("name", new ColumnInfo(false, "Kevin", "Jason"));
		row2.setColumns(columnInfoMap2);
		Assert.assertFalse(batchRow.addRow(row2));
	}
}
