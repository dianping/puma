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

	public void doAfter() throws Exception {
		executeSql("DROP TABLE " + table);
	}
}
