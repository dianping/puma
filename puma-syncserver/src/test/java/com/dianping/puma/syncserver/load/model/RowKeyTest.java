package com.dianping.puma.syncserver.load.model;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.model.RowKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RowKeyTest {

	@Test
	public void testGetOldRowKey() {

		// Case 1: insert.
		RowChangedEvent row0 = new RowChangedEvent();
		row0.setDMLType(DMLType.INSERT);
		row0.setDatabase("puma");
		row0.setTable("test");
		Map<String, ColumnInfo> columnInfoMap0 = new HashMap<String, ColumnInfo>();
		columnInfoMap0.put("id", new ColumnInfo(true, null, 0));
		columnInfoMap0.put("name", new ColumnInfo(false, null, "Linda"));
		row0.setColumns(columnInfoMap0);

		RowKey result0 = RowKey.getOldRowKey(row0);
		RowKey expected0 = new RowKey();
		expected0.setSchema("puma");
		expected0.setTable("test");
		expected0.addPriKey("id", null);

		Assert.assertEquals(expected0, result0);

		// Case 2: delete.
		RowChangedEvent row1 = new RowChangedEvent();
		row1.setDMLType(DMLType.DELETE);
		row1.setDatabase("puma");
		row1.setTable("test");
		Map<String, ColumnInfo> columnInfoMap1 = new HashMap<String, ColumnInfo>();
		columnInfoMap1.put("id", new ColumnInfo(true, null, 0));
		columnInfoMap1.put("name", new ColumnInfo(false, null, "Linda"));
		row1.setColumns(columnInfoMap1);

		RowKey result1 = RowKey.getOldRowKey(row1);
		RowKey expected1 = new RowKey();
		expected1.setSchema("puma");
		expected1.setTable("test");
		expected1.addPriKey("id", null);

		Assert.assertEquals(expected1, result1);

		// Case 3: update.
		RowChangedEvent row2 = new RowChangedEvent();
		row2.setDMLType(DMLType.UPDATE);
		row2.setDatabase("puma");
		row2.setTable("test");
		Map<String, ColumnInfo> columnInfoMap2 = new HashMap<String, ColumnInfo>();
		columnInfoMap2.put("id", new ColumnInfo(true, 0, 0));
		columnInfoMap2.put("name", new ColumnInfo(false, "James", "Linda"));
		row2.setColumns(columnInfoMap2);

		RowKey result2 = RowKey.getOldRowKey(row2);
		RowKey expected2 = new RowKey();
		expected2.setSchema("puma");
		expected2.setTable("test");
		expected2.addPriKey("id", 0);

		Assert.assertEquals(expected2, result2);
	}

	@Test
	public void testGetNewRowKey() {

		// Case 1: insert.
		RowChangedEvent row0 = new RowChangedEvent();
		row0.setDMLType(DMLType.INSERT);
		row0.setDatabase("puma");
		row0.setTable("test");
		Map<String, ColumnInfo> columnInfoMap0 = new HashMap<String, ColumnInfo>();
		columnInfoMap0.put("id", new ColumnInfo(true, null, 0));
		columnInfoMap0.put("name", new ColumnInfo(false, null, "Linda"));
		row0.setColumns(columnInfoMap0);

		RowKey result0 = RowKey.getNewRowKey(row0);
		RowKey expected0 = new RowKey();
		expected0.setSchema("puma");
		expected0.setTable("test");
		expected0.addPriKey("id", 0);

		Assert.assertEquals(expected0, result0);

		// Case 2: delete.
		RowChangedEvent row1 = new RowChangedEvent();
		row1.setDMLType(DMLType.DELETE);
		row1.setDatabase("puma");
		row1.setTable("test");
		Map<String, ColumnInfo> columnInfoMap1 = new HashMap<String, ColumnInfo>();
		columnInfoMap1.put("id", new ColumnInfo(true, null, 0));
		columnInfoMap1.put("name", new ColumnInfo(false, null, "Linda"));
		row1.setColumns(columnInfoMap1);

		RowKey result1 = RowKey.getNewRowKey(row1);
		RowKey expected1 = new RowKey();
		expected1.setSchema("puma");
		expected1.setTable("test");
		expected1.addPriKey("id", 0);

		Assert.assertEquals(expected1, result1);

		// Case 3: update.
		RowChangedEvent row2 = new RowChangedEvent();
		row2.setDMLType(DMLType.UPDATE);
		row2.setDatabase("puma");
		row2.setTable("test");
		Map<String, ColumnInfo> columnInfoMap2 = new HashMap<String, ColumnInfo>();
		columnInfoMap2.put("id", new ColumnInfo(true, 0, 1));
		columnInfoMap2.put("name", new ColumnInfo(false, "James", "Linda"));
		row2.setColumns(columnInfoMap2);

		RowKey result2 = RowKey.getNewRowKey(row2);
		RowKey expected2 = new RowKey();
		expected2.setSchema("puma");
		expected2.setTable("test");
		expected2.addPriKey("id", 1);

		Assert.assertEquals(expected2, result2);
	}
}
