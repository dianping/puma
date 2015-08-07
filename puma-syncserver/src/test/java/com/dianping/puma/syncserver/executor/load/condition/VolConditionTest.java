package com.dianping.puma.syncserver.executor.load.condition;

import com.dianping.puma.syncserver.common.binlog.InsertEvent;
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
		InsertEvent insertEvent = new InsertEvent();

		assertEquals(0, volCondition.count);

		volCondition.lock(insertEvent);

		assertEquals(1, volCondition.count);
	}

	@Test
	public void testUnlock() {
		InsertEvent insertEvent = new InsertEvent();

		assertEquals(0, volCondition.count);

		volCondition.lock(insertEvent);

		assertEquals(1, volCondition.count);

		volCondition.unlock(insertEvent);

		assertEquals(0, volCondition.count);
	}

	@Test
	public void testIsLocked() {
		InsertEvent insertEvent = new InsertEvent();

		assertEquals(0, volCondition.count);

		volCondition.lock(insertEvent);

		assertEquals(1, volCondition.count);

		assertTrue(volCondition.isLocked(insertEvent));
	}
}