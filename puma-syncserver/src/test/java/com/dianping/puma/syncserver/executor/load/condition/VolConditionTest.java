package com.dianping.puma.syncserver.executor.load.condition;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.syncserver.common.binlog.BinlogEvent;
import com.dianping.puma.syncserver.common.binlog.DmlEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class VolConditionTest {

	private VolCondition volCondition = new VolCondition(1);

	@Before
	public void before() {
		volCondition.reset();
	}

	@Test
	public void testLock() {
		DmlEvent dmlEvent = new DmlEvent();

		assertEquals(0, volCondition.count);

		volCondition.lock(dmlEvent);

		assertEquals(1, volCondition.count);
	}

	@Test
	public void testUnlock() {
		DmlEvent dmlEvent = new DmlEvent();

		assertEquals(0, volCondition.count);

		volCondition.lock(dmlEvent);

		assertEquals(1, volCondition.count);

		volCondition.unlock(dmlEvent);

		assertEquals(0, volCondition.count);
	}

	@Test
	public void testIsLocked() {
		DmlEvent dmlEvent = new DmlEvent();

		assertEquals(0, volCondition.count);

		volCondition.lock(dmlEvent);

		assertEquals(1, volCondition.count);

		assertTrue(volCondition.isLocked(dmlEvent));
	}
}