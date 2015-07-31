package com.dianping.puma.syncserver.executor.load.condition;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.common.binlog.Column;
import com.dianping.puma.syncserver.common.binlog.DmlEvent;
import com.dianping.puma.syncserver.common.binlog.EventType;
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
		DmlEvent dmlEvent0 = new DmlEvent();
		dmlEvent0.setEventType(EventType.INSERT);
		dmlEvent0.setDatabase("test-database-0");
		dmlEvent0.setTable("test-table-0");
		dmlEvent0.addColumn("test-column-0", new Column(true, null, 1));
		dmlEvent0.addColumn("test-column-1", new Column(true, null, "aaa"));
		rowCondition.lock(dmlEvent0);

		RowCondition.Row row0 = RowCondition.Row.valueOf(dmlEvent0);

		assertTrue(rowCondition.rowMap.containsKey(row0));

		DmlEvent dmlEvent1 = new DmlEvent();
		dmlEvent1.setEventType(EventType.UPDATE);
		dmlEvent1.setDatabase("test-database-0");
		dmlEvent1.setTable("test-table-0");
		dmlEvent1.addColumn("test-column-0", new Column(true, 2, 2));
		dmlEvent1.addColumn("test-column-1", new Column(true, "aaa", "aaa"));
		rowCondition.lock(dmlEvent1);

		RowCondition.Row row1 = RowCondition.Row.valueOf(dmlEvent1);

		assertTrue(rowCondition.rowMap.containsKey(row1));
	}

	@Test
	public void testUnlock() {
		DmlEvent dmlEvent0 = new DmlEvent();
		dmlEvent0.setEventType(EventType.DELETE);
		dmlEvent0.setDatabase("test-database-0");
		dmlEvent0.setTable("test-table-0");
		dmlEvent0.addColumn("test-column-0", new Column(true, 1, null));
		dmlEvent0.addColumn("test-column-1", new Column(true, "aaa", null));
		rowCondition.lock(dmlEvent0);

		RowCondition.Row row0 = RowCondition.Row.valueOf(dmlEvent0);

		assertTrue(rowCondition.rowMap.containsKey(row0));

		rowCondition.unlock(dmlEvent0);

		assertFalse(rowCondition.rowMap.containsKey(row0));
	}

	@Test
	public void testIsLocked() {
		DmlEvent dmlEvent0 = new DmlEvent();
		dmlEvent0.setEventType(EventType.INSERT);
		dmlEvent0.setDatabase("test-database-0");
		dmlEvent0.setTable("test-table-0");
		dmlEvent0.addColumn("test-column-0", new Column(true, null, 1));
		dmlEvent0.addColumn("test-column-1", new Column(true, null, "aaa"));

		boolean result0 = rowCondition.isLocked(dmlEvent0);
		assertEquals(false, result0);

		rowCondition.lock(dmlEvent0);

		DmlEvent dmlEvent1 = new DmlEvent();
		dmlEvent1.setEventType(EventType.UPDATE);
		dmlEvent1.setDatabase("test-database-0");
		dmlEvent1.setTable("test-table-0");
		dmlEvent1.addColumn("test-column-0", new Column(true, 1, 1));
		dmlEvent1.addColumn("test-column-1", new Column(true, "aaa", "aaa"));

		boolean result1 = rowCondition.isLocked(dmlEvent1);
		assertEquals(true, result1);
	}
}