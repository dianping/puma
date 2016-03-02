package com.dianping.puma.filter;

import com.dianping.puma.core.event.DdlEvent;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DefaultEventFilterChainTest {

	public DefaultEventFilterChain eventFilterChain = new DefaultEventFilterChain();

	public DmlDdlEventFilter eventfilter = new DmlDdlEventFilter();

	public DmlDdlEventFilter eventfilter2 = new DmlDdlEventFilter();

	public List<EventFilter> eventFilters = new ArrayList<EventFilter>();

	@Before
	public void before() {
		eventfilter.init(true, false);
		eventfilter2.init(false, false);
	}

	@Test
	public void testDoNext() {
		eventFilters.add(eventfilter);
		this.eventFilterChain.setEventFilters(eventFilters);
		DdlEvent ddlEvent = new DdlEvent();
		Assert.assertTrue(this.eventFilterChain.doNext(ddlEvent));

		eventFilters.add(eventfilter2);
		Assert.assertFalse(this.eventFilterChain.doNext(ddlEvent));
		eventFilters.remove(eventfilter);
		Assert.assertTrue(this.eventFilterChain.doNext(ddlEvent));
	}

	@Test
	public void testReset() {
		eventFilters.add(eventfilter2);
		this.eventFilterChain.setEventFilters(eventFilters);
		DdlEvent ddlEvent = new DdlEvent();
		Assert.assertFalse(this.eventFilterChain.doNext(ddlEvent));
		eventFilters.remove(eventfilter);
		Assert.assertTrue(this.eventFilterChain.doNext(ddlEvent));
		this.eventFilterChain.reset();
		Assert.assertFalse(this.eventFilterChain.doNext(ddlEvent));

	}

	@After
	public void after() {
		eventFilters.clear();
		eventFilterChain = null;
	}

}
