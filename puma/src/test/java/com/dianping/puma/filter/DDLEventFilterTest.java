package com.dianping.puma.filter;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.model.Table;
import com.dianping.puma.model.TableSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * ddl event type not impl
 */
@Ignore
public class DDLEventFilterTest {

	private DDLEventFilter eventFilter = new DDLEventFilter();

	@Before
	public void before() {
		eventFilter.setName("puma");
		eventFilter.setDdl(true);

		List<DDLType> ddlTypes = new ArrayList<DDLType>();
		ddlTypes.add(DDLType.ALTER_TABLE);
		ddlTypes.add(DDLType.CREATE_INDEX);
		ddlTypes.add(DDLType.DROP_INDEX);
		eventFilter.setDdlTypes(ddlTypes);

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
		// Case 1: ALTER TABLE, "schema.table".
		DdlEvent ddlEvent1 = new DdlEvent();
		ddlEvent1.setDDLType(DDLType.ALTER_TABLE);
		ddlEvent1.setDatabase("schema");
		ddlEvent1.setTable("table");
		Assert.assertTrue(eventFilter.checkEvent(ddlEvent1));

		// Case 2: DROP TABLE, "schema.table".
		DdlEvent ddlEvent2 = new DdlEvent();
		ddlEvent2.setDDLType(DDLType.DROP_TABLE);
		ddlEvent2.setDatabase("schema");
		ddlEvent2.setTable("table");
		Assert.assertFalse(eventFilter.checkEvent(ddlEvent2));

		// Case 3: CREATE INDEX, "puma.hello".
		DdlEvent ddlEvent3 = new DdlEvent();
		ddlEvent3.setDDLType(DDLType.CREATE_INDEX);
		ddlEvent3.setDatabase("puma");
		ddlEvent3.setTable("hello");
		Assert.assertTrue(eventFilter.checkEvent(ddlEvent3));

		// Case 4: DROP INDEX, "puma.world".
		DdlEvent ddlEvent4 = new DdlEvent();
		ddlEvent4.setDDLType(DDLType.DROP_INDEX);
		ddlEvent4.setDatabase("puma");
		ddlEvent4.setTable("world");
		Assert.assertTrue(eventFilter.checkEvent(ddlEvent4));

		// Case 5: DML.
		RowChangedEvent rowChangedEvent1 = new RowChangedEvent();
		rowChangedEvent1.setTransactionBegin(false);
		rowChangedEvent1.setTransactionCommit(false);
		Assert.assertTrue(eventFilter.checkEvent(rowChangedEvent1));

		// Case 6: Transaction begin.
		RowChangedEvent rowChangedEvent2 = new RowChangedEvent();
		rowChangedEvent2.setTransactionBegin(true);
		rowChangedEvent2.setTransactionCommit(false);
		Assert.assertTrue(eventFilter.checkEvent(rowChangedEvent2));

		// Case 7: Transaction commit.
		RowChangedEvent rowChangedEvent3 = new RowChangedEvent();
		rowChangedEvent3.setTransactionBegin(false);
		rowChangedEvent3.setTransactionCommit(true);
		Assert.assertTrue(eventFilter.checkEvent(rowChangedEvent3));
	}
}
