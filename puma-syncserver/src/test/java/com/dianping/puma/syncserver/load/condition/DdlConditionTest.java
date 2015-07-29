package com.dianping.puma.syncserver.load.condition;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
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
		DdlEvent ddlEvent = new DdlEvent();
		ddlEvent.setDatabase("test-database-0");
		ddlEvent.setTable("test-table-0");

		ddlCondition.lock(ddlEvent);
		assertTrue(ddlCondition.ddlEvent.equals(ddlEvent));
	}

	@Test
	public void testUnlock() {
		DdlEvent ddlEvent = new DdlEvent();
		ddlEvent.setDatabase("test-database-0");
		ddlEvent.setTable("test-table-0");

		ddlCondition.lock(ddlEvent);
		assertTrue(ddlCondition.ddlEvent.equals(ddlEvent));

		ddlCondition.unlock(ddlEvent);
		assertNull(ddlCondition.ddlEvent);
	}

	@Test
	public void testIsLocked() {
		DdlEvent ddlEvent = new DdlEvent();
		ddlEvent.setDatabase("test-database-0");
		ddlEvent.setTable("test-table-0");

		ddlCondition.lock(ddlEvent);
		assertTrue(ddlCondition.ddlEvent.equals(ddlEvent));

		RowChangedEvent rowChangedEvent = new RowChangedEvent();
		assertTrue(ddlCondition.isLocked(rowChangedEvent));

		ddlCondition.unlock(ddlEvent);
		assertNull(ddlCondition.ddlEvent);

		assertFalse(ddlCondition.isLocked(rowChangedEvent));
	}

}