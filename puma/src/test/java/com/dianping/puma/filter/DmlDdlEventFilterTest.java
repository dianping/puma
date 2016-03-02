package com.dianping.puma.filter;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import org.junit.Assert;
import org.junit.Test;

public class DmlDdlEventFilterTest {
	public DmlDdlEventFilter eventfilter = new DmlDdlEventFilter();

	public RowChangedEvent changedEvent = new RowChangedEvent();

	public DdlEvent ddlEvent = new DdlEvent();

	@Test
	public void testCheckEvent() {
		eventfilter.init(false, false);
		Assert.assertFalse(eventfilter.checkEvent(changedEvent));
		Assert.assertFalse(eventfilter.checkEvent(ddlEvent));

		eventfilter = new DmlDdlEventFilter();
		eventfilter.init(true, false);
		Assert.assertTrue(eventfilter.checkEvent(ddlEvent));
		Assert.assertFalse(eventfilter.checkEvent(changedEvent));

		eventfilter = new DmlDdlEventFilter();
		eventfilter.init(false, true);
		Assert.assertTrue(eventfilter.checkEvent(changedEvent));
		Assert.assertFalse(eventfilter.checkEvent(ddlEvent));

		eventfilter = new DmlDdlEventFilter();
		eventfilter.init(true, true);
		Assert.assertTrue(eventfilter.checkEvent(changedEvent));
		Assert.assertTrue(eventfilter.checkEvent(ddlEvent));
	}
}
