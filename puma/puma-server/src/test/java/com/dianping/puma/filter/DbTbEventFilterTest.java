package com.dianping.puma.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.event.RowChangedEvent;


public class DbTbEventFilterTest {

	public DbTbEventFilter eventfilter=new DbTbEventFilter();
	
	@Before
	public void before()
	{
		String[] dts={"cat.table1", "cat.table2", "dog.*", "puma.ab* "};
		this.eventfilter.init(dts);
	}
	
	@Test
	public void testCheckEvent()
	{
		RowChangedEvent event=new RowChangedEvent();
		event.setDatabase("cat");
		event.setTable("table1");
		Assert.assertTrue(this.eventfilter.checkEvent(event));
		event = new RowChangedEvent();
		event.setDatabase("cat");
		event.setTable("table3");
		Assert.assertTrue(!this.eventfilter.checkEvent(event));
		event = new RowChangedEvent();
		event.setDatabase("dog");
		event.setTable("table3");
		Assert.assertTrue(this.eventfilter.checkEvent(event));
		event = new RowChangedEvent();
		event.setDatabase("puma");
		event.setTable("table3");
		Assert.assertTrue(!this.eventfilter.checkEvent(event));
		event = new RowChangedEvent();
		event.setDatabase("puma");
		event.setTable("abcd");
		Assert.assertTrue(this.eventfilter.checkEvent(event));
		event = new RowChangedEvent();
		event.setDatabase("pum");
		event.setTable("abcd");
		Assert.assertTrue(!this.eventfilter.checkEvent(event));
		Assert.assertFalse(this.eventfilter.checkEvent(null));
		
	}
	
}
