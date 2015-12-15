package com.dianping.puma.filter;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.model.Schema;
import com.dianping.puma.model.SchemaSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TransactionEventFilterTest {

	private TransactionEventFilter eventFilter = new TransactionEventFilter();

	@Before
	public void before() {
		eventFilter.setName("puma");
		eventFilter.setBegin(false);
		eventFilter.setCommit(true);

		SchemaSet schemaSet = new SchemaSet();
		schemaSet.add(new Schema("puma"));
		schemaSet.add(new Schema("test"));
		eventFilter.setAcceptedSchemas(schemaSet);
	}

	@Test
	public void testCheckEvent() {
		// Case 1.
		RowChangedEvent rowChangedEvent0 = new RowChangedEvent();
		rowChangedEvent0.setDatabase("puma");
		rowChangedEvent0.setTransactionBegin(false);
		rowChangedEvent0.setTransactionCommit(true);
		Assert.assertTrue(eventFilter.checkEvent(rowChangedEvent0));

		// Case 2.
		RowChangedEvent rowChangedEvent1 = new RowChangedEvent();
		rowChangedEvent1.setDatabase("puma");
		rowChangedEvent1.setTransactionBegin(true);
		rowChangedEvent1.setTransactionCommit(false);
		Assert.assertFalse(eventFilter.checkEvent(rowChangedEvent1));

		// Case 3.
		RowChangedEvent rowChangedEvent2 = new RowChangedEvent();
		rowChangedEvent2.setDatabase("hello");
		rowChangedEvent2.setTransactionBegin(false);
		rowChangedEvent2.setTransactionCommit(true);
		Assert.assertFalse(eventFilter.checkEvent(rowChangedEvent2));

		// Case 4: DML.
		RowChangedEvent rowChangedEvent3 = new RowChangedEvent();
		rowChangedEvent3.setDatabase("hello");
		rowChangedEvent3.setTransactionBegin(false);
		rowChangedEvent3.setTransactionCommit(false);
		Assert.assertTrue(eventFilter.checkEvent(rowChangedEvent3));

		// Case 5: DDL.
		DdlEvent ddlEvent = new DdlEvent();
		ddlEvent.setDatabase("hello");
		Assert.assertTrue(eventFilter.checkEvent(ddlEvent));
	}
}
