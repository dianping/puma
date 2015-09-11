package com.dianping.puma.filter;

import java.util.List;

import com.dianping.puma.core.event.ChangedEvent;
import org.junit.Assert;
import org.junit.Test;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;

public class TransactionInfoEventFilterTest {
	public TransactionInfoEventFilter eventFilter = new TransactionInfoEventFilter();

	public RowChangedEvent changedEvent = new RowChangedEvent();

	public EventFilterChainTest filterChain = new EventFilterChainTest();

	class EventFilterChainTest implements EventFilterChain {

		@Override
		public boolean doNext(ChangedEvent event) {
			return true;
		}

		@Override
		public void reset() {

		}

		@Override
		public void setEventFilters(List<EventFilter> eventFilters) {

		}

	}

	@Test
	public void testAccept() {
		changedEvent.setTransactionBegin(true);
		eventFilter.init(true);
		Assert.assertTrue(eventFilter.accept(changedEvent, null));
		changedEvent.setTransactionCommit(true);
		Assert.assertTrue(eventFilter.accept(changedEvent, null));
		DdlEvent ddlEvent = new DdlEvent();
		Assert.assertTrue(eventFilter.accept(ddlEvent, this.filterChain));
		eventFilter.init(false);
		Assert.assertFalse(eventFilter.accept(changedEvent, this.filterChain));
		changedEvent.setTransactionCommit(false);
		changedEvent.setTransactionBegin(false);
		Assert.assertTrue(eventFilter.accept(changedEvent, this.filterChain));
	}

}
