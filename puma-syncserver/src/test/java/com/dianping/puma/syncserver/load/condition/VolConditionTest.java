package com.dianping.puma.syncserver.load.condition;

import com.dianping.puma.core.event.RowChangedEvent;
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
		RowChangedEvent rowChangedEvent = new RowChangedEvent();

		assertEquals(0, volCondition.count);

		volCondition.lock(rowChangedEvent);

		assertEquals(1, volCondition.count);
	}

	@Test
	public void testUnlock() {
		RowChangedEvent rowChangedEvent = new RowChangedEvent();

		assertEquals(0, volCondition.count);

		volCondition.lock(rowChangedEvent);

		assertEquals(1, volCondition.count);

		volCondition.unlock(rowChangedEvent);

		assertEquals(0, volCondition.count);
	}

	@Test
	public void testIsLocked() {
		RowChangedEvent rowChangedEvent = new RowChangedEvent();

		assertEquals(0, volCondition.count);

		volCondition.lock(rowChangedEvent);

		assertEquals(1, volCondition.count);

		assertTrue(volCondition.isLocked(rowChangedEvent));
	}
}