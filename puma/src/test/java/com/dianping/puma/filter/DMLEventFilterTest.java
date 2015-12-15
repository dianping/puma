package com.dianping.puma.filter;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.model.Table;
import com.dianping.puma.model.TableSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DMLEventFilterTest {

	private DMLEventFilter eventFilter = new DMLEventFilter();

	@Before
	public void before() {
		eventFilter.setName("puma");
		eventFilter.setDml(true);

		TableSet tableSet = new TableSet();
		Table table1 = new Table("schema", "table");
		tableSet.add(table1);
		Table table2 = new Table("schema", "test");
		tableSet.add(table2);
		Table table3 = new Table("puma", "*");
		tableSet.add(table3);
		eventFilter.setAcceptedTables(tableSet);
	}

	@Test
	public void testCheckEvent() {
		// Case 1: `schema.table`.
		RowChangedEvent rowChangedEvent1 = new RowChangedEvent();
		rowChangedEvent1.setDatabase("schema");
		rowChangedEvent1.setTable("table");

		Assert.assertTrue(eventFilter.checkEvent(rowChangedEvent1));

		// Case 2: `schema.test`.
		RowChangedEvent rowChangedEvent2 = new RowChangedEvent();
		rowChangedEvent2.setDatabase("schema");
		rowChangedEvent2.setTable("test");
		Assert.assertTrue(eventFilter.checkEvent(rowChangedEvent2));

		// Case 3: `schema.bb`.
		RowChangedEvent rowChangedEvent3 = new RowChangedEvent();
		rowChangedEvent3.setDatabase("schema");
		rowChangedEvent3.setTable("bb");
		Assert.assertFalse(eventFilter.checkEvent(rowChangedEvent3));

		// Case 4: `puma.table`.
		RowChangedEvent rowChangedEvent4 = new RowChangedEvent();
		rowChangedEvent4.setDatabase("puma");
		rowChangedEvent4.setTable("table");
		Assert.assertTrue(eventFilter.checkEvent(rowChangedEvent4));

		// Case 5: `puma.null`
		RowChangedEvent rowChangedEvent5 = new RowChangedEvent();
		rowChangedEvent5.setDatabase("puma");
		rowChangedEvent5.setTable(null);
		Assert.assertFalse(eventFilter.checkEvent(rowChangedEvent5));

		// Case 6: transaction begin.
		RowChangedEvent rowChangedEvent6 = new RowChangedEvent();
		rowChangedEvent6.setTransactionBegin(true);
		rowChangedEvent6.setTransactionCommit(false);
		rowChangedEvent6.setDatabase("nope");
		rowChangedEvent6.setTable("nope");
		Assert.assertTrue(eventFilter.checkEvent(rowChangedEvent6));

		// Case 7: transaction commit.
		RowChangedEvent rowChangedEvent7 = new RowChangedEvent();
		rowChangedEvent7.setTransactionBegin(false);
		rowChangedEvent7.setTransactionCommit(true);
		rowChangedEvent7.setDatabase("nope");
		rowChangedEvent7.setTable("nope");
		Assert.assertTrue(eventFilter.checkEvent(rowChangedEvent7));

		// Case 8: ddl.
		DdlEvent ddlEvent = new DdlEvent();
		ddlEvent.setDatabase("nope");
		ddlEvent.setTable("nope");
		Assert.assertTrue(eventFilter.checkEvent(ddlEvent));
	}
}
