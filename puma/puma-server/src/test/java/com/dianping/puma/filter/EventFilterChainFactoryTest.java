package com.dianping.puma.filter;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.puma.core.event.RowChangedEvent;


public class EventFilterChainFactoryTest {

	@Test
	public void testCreateEventFilterChain()
	{
		String[] dts={"cat.table1", "cat.table2", "dog.*", "puma.ab* "};
		EventFilterChain filterChain= EventFilterChainFactory.createEventFilterChain(true, true, false, dts);
		RowChangedEvent event=new RowChangedEvent();
		event.setDatabase("cat");
		event.setTable("table1");
		Assert.assertTrue(filterChain.doNext(event));
		event.setTransactionBegin(true);
		filterChain.reset();
		Assert.assertFalse(filterChain.doNext(event));
		filterChain= EventFilterChainFactory.createEventFilterChain(true, false, true, dts);
		Assert.assertTrue(filterChain.doNext(event));
		
		
	}
	
}
