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
	public void testDDl() throws Exception {
		executeSql("DROP TABLE IF EXISTS DDLtest");
		waitForSync(50);
		test(new TestLogic() {

			@Override
			public void doLogic() throws Exception {
				executeSql("CREATE TABLE DDLtest(id INT)");
				waitForSync(1000);
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

	public void doAfter() throws Exception {
		executeSql("DROP TABLE " + table);
	}
}
