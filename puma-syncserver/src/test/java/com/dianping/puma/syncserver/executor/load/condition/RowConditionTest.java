package com.dianping.puma.syncserver.executor.load.condition;

import com.dianping.puma.syncserver.common.binlog.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RowConditionTest {

	private RowCondition rowCondition = new RowCondition();

	@Before
	public void before() {
		rowCondition.reset();
	}

	@Test
	public void testLock() {
		InsertEvent insertEvent = new InsertEvent();
		insertEvent.setDatabase("test-database-0");
		insertEvent.setTable("test-table-0");
		insertEvent.addColumn("test-column-0", new Column(true, null, 1));
		insertEvent.addColumn("test-column-1", new Column(true, null, "aaa"));
		rowCondition.lock(insertEvent);

		RowCondition.Row row0 = RowCondition.Row.valueOf(insertEvent);

		assertTrue(rowCondition.rowMap.containsKey(row0));

		UpdateEvent updateEvent = new UpdateEvent();
		updateEvent.setDatabase("test-database-0");
		updateEvent.setTable("test-table-0");
		updateEvent.addColumn("test-column-0", new Column(true, 2, 2));
		updateEvent.addColumn("test-column-1", new Column(true, "aaa", "aaa"));
		rowCondition.lock(updateEvent);

		RowCondition.Row row1 = RowCondition.Row.valueOf(updateEvent);

		assertTrue(rowCondition.rowMap.containsKey(row1));
	}

	@Test
	public void testUnlock() {
		DeleteEvent deleteEvent = new DeleteEvent();
		deleteEvent.setDatabase("test-database-0");
		deleteEvent.setTable("test-table-0");
		deleteEvent.addColumn("test-column-0", new Column(true, 1, null));
		deleteEvent.addColumn("test-column-1", new Column(true, "aaa", null));
		rowCondition.lock(deleteEvent);

		RowCondition.Row row0 = RowCondition.Row.valueOf(deleteEvent);

		assertTrue(rowCondition.rowMap.containsKey(row0));

		rowCondition.unlock(deleteEvent);

		assertFalse(rowCondition.rowMap.containsKey(row0));
	}

	@Test
	public void testIsLocked() {
		InsertEvent insertEvent = new InsertEvent();
		insertEvent.setDatabase("test-database-0");
		insertEvent.setTable("test-table-0");
		insertEvent.addColumn("test-column-0", new Column(true, null, 1));
		insertEvent.addColumn("test-column-1", new Column(true, null, "aaa"));

		boolean result0 = rowCondition.isLocked(insertEvent);
		assertEquals(false, result0);

		rowCondition.lock(insertEvent);

		UpdateEvent updateEvent = new UpdateEvent();
		updateEvent.setDatabase("test-database-0");
		updateEvent.setTable("test-table-0");
		updateEvent.addColumn("test-column-0", new Column(true, 1, 1));
		updateEvent.addColumn("test-column-1", new Column(true, "aaa", "aaa"));

		boolean result1 = rowCondition.isLocked(updateEvent);
		assertEquals(true, result1);
	}
}