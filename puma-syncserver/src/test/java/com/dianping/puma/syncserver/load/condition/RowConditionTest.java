package com.dianping.puma.syncserver.load.condition;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RowConditionTest {

	private RowCondition rowCondition = new RowCondition();

	@Before
	public void before() {
		rowCondition.reset();
	}

	@Test
	public void testLock() {
		RowChangedEvent rowChangedEvent0 = new RowChangedEvent();
		rowChangedEvent0.setDmlType(DMLType.INSERT);
		rowChangedEvent0.setDatabase("test-database-0");
		rowChangedEvent0.setTable("test-table-0");
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap0 = new HashMap<String, RowChangedEvent.ColumnInfo>();
		columnInfoMap0.put("test-column-0", new RowChangedEvent.ColumnInfo(true, null, 1));
		columnInfoMap0.put("test-column-1", new RowChangedEvent.ColumnInfo(true, null, "aaa"));
		rowChangedEvent0.setColumns(columnInfoMap0);
		rowCondition.lock(rowChangedEvent0);

		RowCondition.Row row0 = RowCondition.Row.valueOf(rowChangedEvent0);

		assertTrue(rowCondition.rowMap.containsKey(row0));

		RowChangedEvent rowChangedEvent1 = new RowChangedEvent();
		rowChangedEvent1.setDmlType(DMLType.UPDATE);
		rowChangedEvent1.setDatabase("test-database-0");
		rowChangedEvent1.setTable("test-table-0");
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap1 = new HashMap<String, RowChangedEvent.ColumnInfo>();
		columnInfoMap1.put("test-column-0", new RowChangedEvent.ColumnInfo(true, 2, 2));
		columnInfoMap1.put("test-column-1", new RowChangedEvent.ColumnInfo(true, "aaa", "aaa"));
		rowChangedEvent1.setColumns(columnInfoMap1);
		rowCondition.lock(rowChangedEvent1);

		RowCondition.Row row1 = RowCondition.Row.valueOf(rowChangedEvent1);

		assertTrue(rowCondition.rowMap.containsKey(row1));
	}

	@Test
	public void testUnlock() {
		RowChangedEvent rowChangedEvent0 = new RowChangedEvent();
		rowChangedEvent0.setDmlType(DMLType.DELETE);
		rowChangedEvent0.setDatabase("test-database-0");
		rowChangedEvent0.setTable("test-table-0");
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap0 = new HashMap<String, RowChangedEvent.ColumnInfo>();
		columnInfoMap0.put("test-column-0", new RowChangedEvent.ColumnInfo(true, 1, null));
		columnInfoMap0.put("test-column-1", new RowChangedEvent.ColumnInfo(true, "aaa", null));
		rowChangedEvent0.setColumns(columnInfoMap0);
		rowCondition.lock(rowChangedEvent0);

		RowCondition.Row row0 = RowCondition.Row.valueOf(rowChangedEvent0);

		assertTrue(rowCondition.rowMap.containsKey(row0));

		rowCondition.unlock(rowChangedEvent0);

		assertFalse(rowCondition.rowMap.containsKey(row0));
	}

	@Test
	public void testIsLocked() {
		RowChangedEvent rowChangedEvent0 = new RowChangedEvent();
		rowChangedEvent0.setDmlType(DMLType.INSERT);
		rowChangedEvent0.setDatabase("test-database-0");
		rowChangedEvent0.setTable("test-table-0");
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap0 = new HashMap<String, RowChangedEvent.ColumnInfo>();
		columnInfoMap0.put("test-column-0", new RowChangedEvent.ColumnInfo(true, null, 1));
		columnInfoMap0.put("test-column-1", new RowChangedEvent.ColumnInfo(true, null, "aaa"));
		rowChangedEvent0.setColumns(columnInfoMap0);

		boolean expected0 = false;
		boolean result0 = rowCondition.isLocked(rowChangedEvent0);
		assertEquals(expected0, result0);

		rowCondition.lock(rowChangedEvent0);

		RowChangedEvent rowChangedEvent1 = new RowChangedEvent();
		rowChangedEvent1.setDmlType(DMLType.UPDATE);
		rowChangedEvent1.setDatabase("test-database-0");
		rowChangedEvent1.setTable("test-table-0");
		Map<String, RowChangedEvent.ColumnInfo> columnInfoMap1 = new HashMap<String, RowChangedEvent.ColumnInfo>();
		columnInfoMap1.put("test-column-0", new RowChangedEvent.ColumnInfo(true, 1, 1));
		columnInfoMap1.put("test-column-1", new RowChangedEvent.ColumnInfo(true, "aaa", "aaa"));
		rowChangedEvent1.setColumns(columnInfoMap1);

		boolean expected1 = true;
		boolean result1 = rowCondition.isLocked(rowChangedEvent1);
		assertEquals(expected1, result1);
	}
}