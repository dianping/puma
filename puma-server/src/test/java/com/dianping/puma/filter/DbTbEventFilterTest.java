package com.dianping.puma.filter;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.puma.core.event.RowChangedEvent;

public class DbTbEventFilterTest {

	public DbTbEventFilter eventFilter = new DbTbEventFilter();

	@Test
	public void testCheckEvent()
	{
		String[] dts={"cat.table1", "cat.table2", "dog.*", "puma.ab* "};
		this.eventFilter.init(dts);

		RowChangedEvent event=new RowChangedEvent();
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

	@Test
	public void testAddAcceptedTables() {
		SchemaTableSet schemaTableSet = new SchemaTableSet();
		SchemaTable schemaTable0 = new SchemaTable("schema", "table");
		schemaTableSet.add(schemaTable0);
		SchemaTable schemaTable1 = new SchemaTable("schema", "puma");
		schemaTableSet.add(schemaTable1);
		SchemaTable schemaTable2 = new SchemaTable("database", "*");
		schemaTableSet.add(schemaTable2);

		eventFilter.addAcceptedTables(schemaTableSet);

		RowChangedEvent rowChangedEvent = new RowChangedEvent();

		// Case 1.
		rowChangedEvent.setDatabase("schema");
		rowChangedEvent.setTable("table");
		Assert.assertTrue(eventFilter.checkEvent(rowChangedEvent));

		// Case 2.
		rowChangedEvent.setDatabase("schema");
		rowChangedEvent.setTable(null);
		Assert.assertFalse(eventFilter.checkEvent(rowChangedEvent));

		// Case 3.
		rowChangedEvent.setDatabase("database");
		rowChangedEvent.setTable("hello");
		Assert.assertTrue(eventFilter.checkEvent(rowChangedEvent));

		// Case 4.
		rowChangedEvent.setDatabase("database");
		rowChangedEvent.setTable(null);
		Assert.assertFalse(eventFilter.checkEvent(rowChangedEvent));

		// Case 5.
		rowChangedEvent.setDatabase("database1");
		rowChangedEvent.setTable("table");
		Assert.assertFalse(eventFilter.checkEvent(rowChangedEvent));
	}

	@Test
	public void testOnEvent() {
		EventCenter eventCenter = new EventCenter();
		eventCenter.init();
		DbTbEventFilter dbTbEventFilter = mock(DbTbEventFilter.class);
		dbTbEventFilter.setName("puma");
		eventCenter.register(dbTbEventFilter);

		AcceptedTableChangedEvent event = new AcceptedTableChangedEvent();
		event.setName("puma");
		eventCenter.post(event);

		verify(dbTbEventFilter, times(1)).onEvent(event);
	}
}
