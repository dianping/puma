package com.dianping.puma.syncserver.executor.load.condition;

import com.dianping.puma.syncserver.common.binlog.BinlogEvent;
import com.dianping.puma.syncserver.common.binlog.DdlEvent;
import com.dianping.puma.syncserver.common.binlog.InsertEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DdlConditionTest {

	private DdlCondition ddlCondition = new DdlCondition();

	@Before
	public void before() {
		ddlCondition.reset();
	}

	@Test
	public void testLock() {
		BinlogEvent ddlEvent = new DdlEvent();
		ddlEvent.setDatabase("test-database-0");
		ddlEvent.setTable("test-table-0");

		ddlCondition.lock(ddlEvent);
		assertTrue(ddlCondition.ddlEvent.equals(ddlEvent));
	}

	@Test
	public void testUnlock() {
		BinlogEvent ddlEvent = new DdlEvent();
		ddlEvent.setDatabase("test-database-0");
		ddlEvent.setTable("test-table-0");

		ddlCondition.lock(ddlEvent);
		assertTrue(ddlCondition.ddlEvent.equals(ddlEvent));

		ddlCondition.unlock(ddlEvent);
		assertNull(ddlCondition.ddlEvent);
	}

	@Test
	public void testIsLocked() {
		BinlogEvent ddlEvent = new DdlEvent();
		ddlEvent.setDatabase("test-database-0");
		ddlEvent.setTable("test-table-0");

		ddlCondition.lock(ddlEvent);
		assertTrue(ddlCondition.ddlEvent.equals(ddlEvent));

		InsertEvent insertEvent = new InsertEvent();
		assertTrue(ddlCondition.isLocked(insertEvent));

		ddlCondition.unlock(ddlEvent);
		assertNull(ddlCondition.ddlEvent);

		assertFalse(ddlCondition.isLocked(insertEvent));
	}

}