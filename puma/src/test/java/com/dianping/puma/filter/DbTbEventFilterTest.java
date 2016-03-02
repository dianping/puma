package com.dianping.puma.filter;

import com.dianping.puma.core.event.RowChangedEvent;
import org.junit.Assert;
import org.junit.Test;

public class DbTbEventFilterTest {

	public DbTbEventFilter eventFilter = new DbTbEventFilter();

	@Test
	public void testCheckEvent() {
		String[] dts = { "cat.table1", "cat.table2", "dog.*", "puma.ab* " };
		this.eventFilter.init(dts);

		RowChangedEvent event = new RowChangedEvent();
		event.setDatabase("cat");
		event.setTable("table1");
		Assert.assertTrue(this.eventFilter.checkEvent(event));
		event = new RowChangedEvent();
		event.setDatabase("cat");
		event.setTable("table3");
		Assert.assertTrue(!this.eventFilter.checkEvent(event));
		event = new RowChangedEvent();
		event.setDatabase("dog");
		event.setTable("table3");
		Assert.assertTrue(this.eventFilter.checkEvent(event));
		event = new RowChangedEvent();
		event.setDatabase("puma");
		event.setTable("table3");
		Assert.assertTrue(!this.eventFilter.checkEvent(event));
		event = new RowChangedEvent();
		event.setDatabase("puma");
		event.setTable("abcd");
		Assert.assertTrue(this.eventFilter.checkEvent(event));
		event = new RowChangedEvent();
		event.setDatabase("pum");
		event.setTable("abcd");
		Assert.assertTrue(!this.eventFilter.checkEvent(event));
		Assert.assertFalse(this.eventFilter.checkEvent(null));
	}
}
