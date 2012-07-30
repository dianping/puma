/**
 * Project: puma-server
 * 
 * File Created at 2012-7-26
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.integration;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;

/**
 * @author Leo Liang
 * 
 */
public class SQLIntegegrationTest extends PumaServerIntegrationBaseTest {
	private String	table	= "sqlTest";

	@Before
	public void before() throws Exception {
		executeSql("DROP TABLE IF EXISTS " + table);
		executeSql("CREATE TABLE " + table + "(id INT)");
	}

	@Test
	public void testInsertNoTransaction() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("INSERT INTO " + table + " values(1)");
				List<ChangedEvent> events = getEvents(1, false);
				Assert.assertEquals(1, events.size());
				Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
				Assert.assertEquals(table, rowChangedEvent.getTable());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());
				Assert.assertEquals(1, rowChangedEvent.getColumns().size());
				Assert.assertEquals(1, rowChangedEvent.getColumns().get("id").getNewValue());
				Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());
			}
		});
	}

	@Test
	public void testInsertWithTransaction() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("INSERT INTO " + table + " values(1)");
				List<ChangedEvent> events = getEvents(3, true);
				Assert.assertEquals(3, events.size());

				Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.isTransactionBegin());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());

				Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
				rowChangedEvent = (RowChangedEvent) events.get(1);
				Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
				Assert.assertEquals(table, rowChangedEvent.getTable());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());
				Assert.assertEquals(1, rowChangedEvent.getColumns().size());
				Assert.assertEquals(1, rowChangedEvent.getColumns().get("id").getNewValue());
				Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());

				Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
				rowChangedEvent = (RowChangedEvent) events.get(2);
				Assert.assertTrue(rowChangedEvent.isTransactionCommit());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
			}
		});
	}

	@Test
	public void testInsertMultiRowsWithTransaction() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("INSERT INTO " + table + " values(1),(2)");
				List<ChangedEvent> events = getEvents(4, true);
				Assert.assertEquals(4, events.size());

				Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.isTransactionBegin());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());

				Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
				rowChangedEvent = (RowChangedEvent) events.get(1);
				Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
				Assert.assertEquals(table, rowChangedEvent.getTable());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());
				Assert.assertEquals(1, rowChangedEvent.getColumns().size());
				Assert.assertEquals(1, rowChangedEvent.getColumns().get("id").getNewValue());
				Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());

				Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
				rowChangedEvent = (RowChangedEvent) events.get(2);
				Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
				Assert.assertEquals(table, rowChangedEvent.getTable());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());
				Assert.assertEquals(1, rowChangedEvent.getColumns().size());
				Assert.assertEquals(2, rowChangedEvent.getColumns().get("id").getNewValue());
				Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());

				Assert.assertTrue(events.get(3) instanceof RowChangedEvent);
				rowChangedEvent = (RowChangedEvent) events.get(3);
				Assert.assertTrue(rowChangedEvent.isTransactionCommit());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
			}
		});
	}

	@Test
	public void testUpdateNoTransaction() throws Exception {
		executeSql("INSERT INTO " + table + " values(1)");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("UPDATE " + table + " SET id=2 WHERE id=1");
				List<ChangedEvent> events = getEvents(1, false);
				Assert.assertEquals(1, events.size());
				Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
				Assert.assertEquals(table, rowChangedEvent.getTable());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());
				Assert.assertEquals(1, rowChangedEvent.getColumns().size());
				Assert.assertEquals(2, rowChangedEvent.getColumns().get("id").getNewValue());
				Assert.assertEquals(1, rowChangedEvent.getColumns().get("id").getOldValue());
			}
		});
	}

	@Test
	public void testUpdateWithTransaction() throws Exception {
		executeSql("INSERT INTO " + table + " values(1)");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("UPDATE " + table + " SET id=2 WHERE id=1");
				List<ChangedEvent> events = getEvents(3, true);
				Assert.assertEquals(3, events.size());

				Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.isTransactionBegin());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());

				Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
				rowChangedEvent = (RowChangedEvent) events.get(1);
				Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
				Assert.assertEquals(table, rowChangedEvent.getTable());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());
				Assert.assertEquals(1, rowChangedEvent.getColumns().size());
				Assert.assertEquals(2, rowChangedEvent.getColumns().get("id").getNewValue());
				Assert.assertEquals(1, rowChangedEvent.getColumns().get("id").getOldValue());

				Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
				rowChangedEvent = (RowChangedEvent) events.get(2);
				Assert.assertTrue(rowChangedEvent.isTransactionCommit());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
			}
		});
	}

	@Test
	public void testUpdateMultiRowsWithTransaction() throws Exception {
		executeSql("INSERT INTO " + table + " values(1)");
		executeSql("INSERT INTO " + table + " values(2)");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("UPDATE " + table + " SET id=3");
				List<ChangedEvent> events = getEvents(4, true);
				Assert.assertEquals(4, events.size());

				Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.isTransactionBegin());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());

				Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
				rowChangedEvent = (RowChangedEvent) events.get(1);
				Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
				Assert.assertEquals(table, rowChangedEvent.getTable());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());
				Assert.assertEquals(1, rowChangedEvent.getColumns().size());
				Assert.assertEquals(3, rowChangedEvent.getColumns().get("id").getNewValue());
				Assert.assertEquals(1, rowChangedEvent.getColumns().get("id").getOldValue());

				Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
				rowChangedEvent = (RowChangedEvent) events.get(2);
				Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
				Assert.assertEquals(table, rowChangedEvent.getTable());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());
				Assert.assertEquals(1, rowChangedEvent.getColumns().size());
				Assert.assertEquals(3, rowChangedEvent.getColumns().get("id").getNewValue());
				Assert.assertEquals(2, rowChangedEvent.getColumns().get("id").getOldValue());

				Assert.assertTrue(events.get(3) instanceof RowChangedEvent);
				rowChangedEvent = (RowChangedEvent) events.get(3);
				Assert.assertTrue(rowChangedEvent.isTransactionCommit());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
			}
		});
	}

	@Test
	public void testDeleteNoTransaction() throws Exception {
		executeSql("INSERT INTO " + table + " values(1)");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("DELETE FROM " + table + " WHERE id=1");
				List<ChangedEvent> events = getEvents(1, false);
				Assert.assertEquals(1, events.size());
				Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
				Assert.assertEquals(table, rowChangedEvent.getTable());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());
				Assert.assertEquals(1, rowChangedEvent.getColumns().size());
				Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
				Assert.assertEquals(1, rowChangedEvent.getColumns().get("id").getOldValue());
			}
		});
	}

	@Test
	public void testDeleteWithTransaction() throws Exception {
		executeSql("INSERT INTO " + table + " values(1)");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("DELETE FROM " + table + " WHERE id=1");
				List<ChangedEvent> events = getEvents(3, true);
				Assert.assertEquals(3, events.size());

				Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.isTransactionBegin());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());

				Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
				rowChangedEvent = (RowChangedEvent) events.get(1);
				Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
				Assert.assertEquals(table, rowChangedEvent.getTable());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());
				Assert.assertEquals(1, rowChangedEvent.getColumns().size());
				Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
				Assert.assertEquals(1, rowChangedEvent.getColumns().get("id").getOldValue());

				Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
				rowChangedEvent = (RowChangedEvent) events.get(2);
				Assert.assertTrue(rowChangedEvent.isTransactionCommit());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
			}
		});
	}

	@Test
	public void testDeleteMultipleRowsWithTransaction() throws Exception {
		executeSql("INSERT INTO " + table + " values(1)");
		executeSql("INSERT INTO " + table + " values(2)");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("DELETE FROM " + table);
				List<ChangedEvent> events = getEvents(4, true);
				Assert.assertEquals(4, events.size());

				RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowChangedEvent.isTransactionBegin());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());

				rowChangedEvent = (RowChangedEvent) events.get(1);
				Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
				Assert.assertEquals(table, rowChangedEvent.getTable());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());
				Assert.assertEquals(1, rowChangedEvent.getColumns().size());
				Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
				Assert.assertEquals(1, rowChangedEvent.getColumns().get("id").getOldValue());

				rowChangedEvent = (RowChangedEvent) events.get(2);
				Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
				Assert.assertEquals(table, rowChangedEvent.getTable());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
				Assert.assertEquals(db, rowChangedEvent.getDatabase());
				Assert.assertEquals(1, rowChangedEvent.getColumns().size());
				Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
				Assert.assertEquals(2, rowChangedEvent.getColumns().get("id").getOldValue());

				rowChangedEvent = (RowChangedEvent) events.get(3);
				Assert.assertTrue(rowChangedEvent.isTransactionCommit());
				Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
			}
		});
	}

	@Test
	public void testDDl() throws Exception {
		executeSql("DROP TABLE IF EXISTS DDLtest");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("CREATE TABLE DDLtest(id INT)");
				waitForSync(200);
				List<ChangedEvent> events = getEvents(1, false);
				Assert.assertEquals(1, events.size());
				Assert.assertTrue(events.get(0) instanceof DdlEvent);
				DdlEvent ddlEvent = (DdlEvent) events.get(0);
				Assert.assertEquals(db, ddlEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, ddlEvent.getMasterUrl());
				Assert.assertTrue("CREATE TABLE DDLtest(id INT)".equalsIgnoreCase(ddlEvent.getSql()));
				executeSql("DROP TABLE DDLtest");
			}
		});
	}

	@Test
	public void testMixed() throws Exception {
		executeSql("DROP TABLE IF EXISTS DDLtest");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("INSERT INTO " + table + " values(1)");
				executeSql("UPDATE " + table + " SET id=2 WHERE id=1");
				executeSql("DELETE FROM " + table + " WHERE id=2");
				executeSql("CREATE TABLE DDLtest(id INT)");
				waitForSync(100);
				List<ChangedEvent> events = getEvents(4, false);
				Assert.assertEquals(4, events.size());
				RowChangedEvent rowEvent = (RowChangedEvent) events.get(0);
				Assert.assertEquals(db, rowEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(RowChangedEvent.INSERT, rowEvent.getActionType());
				Assert.assertEquals(table, rowEvent.getTable());
				Assert.assertEquals(1, rowEvent.getColumns().size());
				Assert.assertNull(rowEvent.getColumns().get("id").getOldValue());
				Assert.assertEquals(1, rowEvent.getColumns().get("id").getNewValue());

				rowEvent = (RowChangedEvent) events.get(1);
				Assert.assertEquals(db, rowEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(RowChangedEvent.UPDATE, rowEvent.getActionType());
				Assert.assertEquals(table, rowEvent.getTable());
				Assert.assertEquals(1, rowEvent.getColumns().size());
				Assert.assertEquals(1, rowEvent.getColumns().get("id").getOldValue());
				Assert.assertEquals(2, rowEvent.getColumns().get("id").getNewValue());

				rowEvent = (RowChangedEvent) events.get(2);
				Assert.assertEquals(db, rowEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(RowChangedEvent.DELETE, rowEvent.getActionType());
				Assert.assertEquals(table, rowEvent.getTable());
				Assert.assertEquals(1, rowEvent.getColumns().size());
				Assert.assertEquals(2, rowEvent.getColumns().get("id").getOldValue());
				Assert.assertNull(rowEvent.getColumns().get("id").getNewValue());

				DdlEvent ddlEvent = (DdlEvent) events.get(3);
				Assert.assertEquals(db, ddlEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, ddlEvent.getMasterUrl());
				Assert.assertTrue("CREATE TABLE DDLtest(id INT)".equalsIgnoreCase(ddlEvent.getSql()));
				executeSql("DROP TABLE DDLtest");
			}
		});
	}

	@Test
	public void testMixedWithTransaction() throws Exception {
		executeSql("DROP TABLE IF EXISTS DDLtest");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("INSERT INTO " + table + " values(1)");
				executeSql("UPDATE " + table + " SET id=2 WHERE id=1");
				executeSql("DELETE FROM " + table + " WHERE id=2");
				executeSql("CREATE TABLE DDLtest(id INT)");
				waitForSync(100);
				List<ChangedEvent> events = getEvents(10, true);
				Assert.assertEquals(10, events.size());

				RowChangedEvent rowEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowEvent.isTransactionBegin());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(db, rowEvent.getDatabase());

				rowEvent = (RowChangedEvent) events.get(1);
				Assert.assertEquals(db, rowEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(RowChangedEvent.INSERT, rowEvent.getActionType());
				Assert.assertEquals(table, rowEvent.getTable());
				Assert.assertEquals(1, rowEvent.getColumns().size());
				Assert.assertNull(rowEvent.getColumns().get("id").getOldValue());
				Assert.assertEquals(1, rowEvent.getColumns().get("id").getNewValue());

				rowEvent = (RowChangedEvent) events.get(2);
				Assert.assertTrue(rowEvent.isTransactionCommit());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());

				rowEvent = (RowChangedEvent) events.get(3);
				Assert.assertTrue(rowEvent.isTransactionBegin());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(db, rowEvent.getDatabase());

				rowEvent = (RowChangedEvent) events.get(4);
				Assert.assertEquals(db, rowEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(RowChangedEvent.UPDATE, rowEvent.getActionType());
				Assert.assertEquals(table, rowEvent.getTable());
				Assert.assertEquals(1, rowEvent.getColumns().size());
				Assert.assertEquals(1, rowEvent.getColumns().get("id").getOldValue());
				Assert.assertEquals(2, rowEvent.getColumns().get("id").getNewValue());

				rowEvent = (RowChangedEvent) events.get(5);
				Assert.assertTrue(rowEvent.isTransactionCommit());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());

				rowEvent = (RowChangedEvent) events.get(6);
				Assert.assertTrue(rowEvent.isTransactionBegin());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(db, rowEvent.getDatabase());

				rowEvent = (RowChangedEvent) events.get(7);
				Assert.assertEquals(db, rowEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(RowChangedEvent.DELETE, rowEvent.getActionType());
				Assert.assertEquals(table, rowEvent.getTable());
				Assert.assertEquals(1, rowEvent.getColumns().size());
				Assert.assertEquals(2, rowEvent.getColumns().get("id").getOldValue());
				Assert.assertNull(rowEvent.getColumns().get("id").getNewValue());

				rowEvent = (RowChangedEvent) events.get(8);
				Assert.assertTrue(rowEvent.isTransactionCommit());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());

				DdlEvent ddlEvent = (DdlEvent) events.get(9);
				Assert.assertEquals(db, ddlEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, ddlEvent.getMasterUrl());
				Assert.assertTrue("CREATE TABLE DDLtest(id INT)".equalsIgnoreCase(ddlEvent.getSql()));
				executeSql("DROP TABLE DDLtest");
			}
		});
	}

	@Test
	public void testMixedWithTransaction2() throws Exception {
		executeSql("DROP TABLE IF EXISTS DDLtest");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSqlWithTransaction(Arrays.asList(new String[] { "INSERT INTO " + table + " values(1)",
						"UPDATE " + table + " SET id=2 WHERE id=1", "DELETE FROM " + table + " WHERE id=2",
						"CREATE TABLE DDLtest(id INT)" }));
				waitForSync(100);
				List<ChangedEvent> events = getEvents(6, true);
				Assert.assertEquals(6, events.size());

				RowChangedEvent rowEvent = (RowChangedEvent) events.get(0);
				Assert.assertTrue(rowEvent.isTransactionBegin());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(db, rowEvent.getDatabase());

				rowEvent = (RowChangedEvent) events.get(1);
				Assert.assertEquals(db, rowEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(RowChangedEvent.INSERT, rowEvent.getActionType());
				Assert.assertEquals(table, rowEvent.getTable());
				Assert.assertEquals(1, rowEvent.getColumns().size());
				Assert.assertNull(rowEvent.getColumns().get("id").getOldValue());
				Assert.assertEquals(1, rowEvent.getColumns().get("id").getNewValue());

				rowEvent = (RowChangedEvent) events.get(2);
				Assert.assertEquals(db, rowEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(RowChangedEvent.UPDATE, rowEvent.getActionType());
				Assert.assertEquals(table, rowEvent.getTable());
				Assert.assertEquals(1, rowEvent.getColumns().size());
				Assert.assertEquals(1, rowEvent.getColumns().get("id").getOldValue());
				Assert.assertEquals(2, rowEvent.getColumns().get("id").getNewValue());

				rowEvent = (RowChangedEvent) events.get(3);
				Assert.assertEquals(db, rowEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());
				Assert.assertEquals(RowChangedEvent.DELETE, rowEvent.getActionType());
				Assert.assertEquals(table, rowEvent.getTable());
				Assert.assertEquals(1, rowEvent.getColumns().size());
				Assert.assertEquals(2, rowEvent.getColumns().get("id").getOldValue());
				Assert.assertNull(rowEvent.getColumns().get("id").getNewValue());

				rowEvent = (RowChangedEvent) events.get(4);
				Assert.assertTrue(rowEvent.isTransactionCommit());
				Assert.assertEquals(host + ":" + port, rowEvent.getMasterUrl());

				DdlEvent ddlEvent = (DdlEvent) events.get(5);
				Assert.assertEquals(db, ddlEvent.getDatabase());
				Assert.assertEquals(host + ":" + port, ddlEvent.getMasterUrl());
				Assert.assertTrue("CREATE TABLE DDLtest(id INT)".equalsIgnoreCase(ddlEvent.getSql()));
				executeSql("DROP TABLE DDLtest");
			}
		});
	}

	@Test
	public void testMixedBatch() throws Exception {
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				int count = 3000;
				for (int i = 0; i < count; i++) {
					if (i % 3 == 0) {
						executeSql("INSERT INTO " + table + " values(1)");
					} else if (i % 3 == 1) {
						executeSql("UPDATE " + table + " SET id=2 WHERE id=1");
					} else {
						executeSql("DELETE FROM " + table + " WHERE id=2");
					}
				}

				List<ChangedEvent> events = getEvents(3 * count, true);
				for (int i = 0; i < 3 * count; i += 3) {
					if ((i / 3) % 3 == 0) {
						Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
						RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
						Assert.assertTrue(rowChangedEvent.isTransactionBegin());
						Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
						Assert.assertEquals(db, rowChangedEvent.getDatabase());

						Assert.assertTrue(events.get(i + 1) instanceof RowChangedEvent);
						rowChangedEvent = (RowChangedEvent) events.get(i + 1);
						Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
						Assert.assertEquals(table, rowChangedEvent.getTable());
						Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
						Assert.assertEquals(db, rowChangedEvent.getDatabase());
						Assert.assertEquals(1, rowChangedEvent.getColumns().size());
						Assert.assertEquals(1, rowChangedEvent.getColumns().get("id").getNewValue());
						Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());

						Assert.assertTrue(events.get(i + 2) instanceof RowChangedEvent);
						rowChangedEvent = (RowChangedEvent) events.get(i + 2);
						Assert.assertTrue(rowChangedEvent.isTransactionCommit());
						Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
					} else if ((i / 3) % 3 == 1) {
						Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
						RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
						Assert.assertTrue(rowChangedEvent.isTransactionBegin());
						Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
						Assert.assertEquals(db, rowChangedEvent.getDatabase());

						Assert.assertTrue(events.get(i + 1) instanceof RowChangedEvent);
						rowChangedEvent = (RowChangedEvent) events.get(i + 1);
						Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
						Assert.assertEquals(table, rowChangedEvent.getTable());
						Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
						Assert.assertEquals(db, rowChangedEvent.getDatabase());
						Assert.assertEquals(1, rowChangedEvent.getColumns().size());
						Assert.assertEquals(2, rowChangedEvent.getColumns().get("id").getNewValue());
						Assert.assertEquals(1, rowChangedEvent.getColumns().get("id").getOldValue());

						Assert.assertTrue(events.get(i + 2) instanceof RowChangedEvent);
						rowChangedEvent = (RowChangedEvent) events.get(i + 2);
						Assert.assertTrue(rowChangedEvent.isTransactionCommit());
						Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
					} else {
						Assert.assertTrue(events.get(i) instanceof RowChangedEvent);
						RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(i);
						Assert.assertTrue(rowChangedEvent.isTransactionBegin());
						Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
						Assert.assertEquals(db, rowChangedEvent.getDatabase());

						Assert.assertTrue(events.get(i + 1) instanceof RowChangedEvent);
						rowChangedEvent = (RowChangedEvent) events.get(i + 1);
						Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
						Assert.assertEquals(table, rowChangedEvent.getTable());
						Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
						Assert.assertEquals(db, rowChangedEvent.getDatabase());
						Assert.assertEquals(1, rowChangedEvent.getColumns().size());
						Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
						Assert.assertEquals(2, rowChangedEvent.getColumns().get("id").getOldValue());

						Assert.assertTrue(events.get(i + 2) instanceof RowChangedEvent);
						rowChangedEvent = (RowChangedEvent) events.get(i + 2);
						Assert.assertTrue(rowChangedEvent.isTransactionCommit());
						Assert.assertEquals(host + ":" + port, rowChangedEvent.getMasterUrl());
					}
				}
			}
		});
	}

	public void doAfter() throws Exception {
		executeSql("DROP TABLE " + table);
	}
}
