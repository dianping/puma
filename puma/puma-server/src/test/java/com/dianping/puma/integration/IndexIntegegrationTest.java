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

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;

/**
 * @author Leo Liang
 * 
 */
public class IndexIntegegrationTest extends PumaServerIntegrationBaseTest {
    private String table = "indexTest";

    @Before
    public void before() throws Exception {
        executeSql("DROP TABLE IF EXISTS " + table);
        executeSql("CREATE TABLE " + table + "(id INT)");
    }

    @Test
    public void testInsertOneRowByBinlog() throws Exception {
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                BinlogInfo latestBinlogInfo = getLatestBinlogInfo();
                executeSql("INSERT INTO " + table + " values(1)");
                List<ChangedEvent> events = getEvents(1, SubscribeConstant.SEQ_FROM_BINLOGINFO, serverId,
                        latestBinlogInfo.getFile(), latestBinlogInfo.getPos(), -1, false);
                Assert.assertEquals(1, events.size());
                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());
            }
        });
    }

    @Test
    public void testInsertOneRowTimestamp() throws Exception {
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                long timestamp = new Date().getTime() / 1000;
                executeSql("INSERT INTO " + table + " values(1)");
                waitForSync(4000);
                List<ChangedEvent> events = getEvents(1, SubscribeConstant.SEQ_FROM_TIMESTAMP, -1, null, -1, timestamp,
                        false);
                Assert.assertEquals(1, events.size());
                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());
            }
        });
    }

    @Test
    public void testInsertOneRowWithTransactionByBinlog() throws Exception {
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                BinlogInfo latestBinlogInfo = getLatestBinlogInfo();
                executeSql("INSERT INTO " + table + " values(1)");
                List<ChangedEvent> events = getEvents(3, SubscribeConstant.SEQ_FROM_BINLOGINFO, serverId,
                        latestBinlogInfo.getFile(), latestBinlogInfo.getPos(), -1, true);
                Assert.assertEquals(3, events.size());

                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertTrue(rowChangedEvent.isTransactionBegin());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());

                Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(1);
                Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(2);
                Assert.assertTrue(rowChangedEvent.isTransactionCommit());
            }
        });
    }

    @Test
    public void testInsertOneRowWithTransactionByTimestamp() throws Exception {
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                long timestamp = new Date().getTime() / 1000;
                executeSql("INSERT INTO " + table + " values(1)");
                waitForSync(200);
                List<ChangedEvent> events = getEvents(3, SubscribeConstant.SEQ_FROM_TIMESTAMP, -1, null, -1, timestamp,
                        true);
                Assert.assertEquals(3, events.size());

                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertTrue(rowChangedEvent.isTransactionBegin());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());

                Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(1);
                Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(2);
                Assert.assertTrue(rowChangedEvent.isTransactionCommit());
            }
        });
    }

    @Test
    public void testInsertMultiRowsWithTransactionByBinlog() throws Exception {
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                BinlogInfo latestBinlogInfo = getLatestBinlogInfo();
                executeSql("INSERT INTO " + table + " values(1),(2)");
                List<ChangedEvent> events = getEvents(4, SubscribeConstant.SEQ_FROM_BINLOGINFO, serverId,
                        latestBinlogInfo.getFile(), latestBinlogInfo.getPos(), -1, true);
                Assert.assertEquals(4, events.size());

                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertTrue(rowChangedEvent.isTransactionBegin());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());

                Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(1);
                Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(2);
                Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(2L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(3) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(3);
                Assert.assertTrue(rowChangedEvent.isTransactionCommit());
            }
        });
    }

    @Test
    public void testInsertMultiRowsWithTransactionByTimestamp() throws Exception {
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                long timestamp = new Date().getTime() / 1000;
                executeSql("INSERT INTO " + table + " values(1),(2)");
                waitForSync(5000);
                List<ChangedEvent> events = getEvents(4, SubscribeConstant.SEQ_FROM_TIMESTAMP, -1, null, -1, timestamp,
                        true);
                Assert.assertEquals(4, events.size());

                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertTrue(rowChangedEvent.isTransactionBegin());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());

                Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(1);
                Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(2);
                Assert.assertEquals(RowChangedEvent.INSERT, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(2L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(3) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(3);
                Assert.assertTrue(rowChangedEvent.isTransactionCommit());
            }
        });
    }

    @Test
    public void testUpdateNoTransactionByBinlog() throws Exception {
        executeSql("INSERT INTO " + table + " values(1)");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                BinlogInfo latestBinlogInfo = getLatestBinlogInfo();
                executeSql("UPDATE " + table + " SET id=2 WHERE id=1");
                List<ChangedEvent> events = getEvents(1, SubscribeConstant.SEQ_FROM_BINLOGINFO, serverId,
                        latestBinlogInfo.getFile(), latestBinlogInfo.getPos(), -1, false);
                Assert.assertEquals(1, events.size());
                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(2L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getOldValue());
            }
        });
    }

    @Test
    public void testUpdateNoTransactionByTimestamp() throws Exception {
        executeSql("INSERT INTO " + table + " values(1)");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                long timestamp = new Date().getTime() / 1000;
                executeSql("UPDATE " + table + " SET id=2 WHERE id=1");
                waitForSync(4000);
                List<ChangedEvent> events = getEvents(1, SubscribeConstant.SEQ_FROM_TIMESTAMP, -1, null, -1, timestamp,
                        false);
                Assert.assertEquals(1, events.size());
                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(2L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getOldValue());
            }
        });
    }

    @Test
    public void testUpdateWithTransactionByBinlog() throws Exception {
        executeSql("INSERT INTO " + table + " values(1)");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                BinlogInfo latestBinlogInfo = getLatestBinlogInfo();
                executeSql("UPDATE " + table + " SET id=2 WHERE id=1");
                List<ChangedEvent> events = getEvents(3, SubscribeConstant.SEQ_FROM_BINLOGINFO, serverId,
                        latestBinlogInfo.getFile(), latestBinlogInfo.getPos(), -1, true);
                Assert.assertEquals(3, events.size());

                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertTrue(rowChangedEvent.isTransactionBegin());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());

                Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(1);
                Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(2L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(2);
                Assert.assertTrue(rowChangedEvent.isTransactionCommit());
            }
        });
    }

    @Test
    public void testUpdateWithTransactionByTimestamp() throws Exception {
        executeSql("INSERT INTO " + table + " values(1)");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                long timestamp = new Date().getTime() / 1000;
                executeSql("UPDATE " + table + " SET id=2 WHERE id=1");
                waitForSync(5000);
                List<ChangedEvent> events = getEvents(3, SubscribeConstant.SEQ_FROM_TIMESTAMP, -1, null, -1, timestamp,
                        true);
                Assert.assertEquals(3, events.size());

                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertTrue(rowChangedEvent.isTransactionBegin());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());

                Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(1);
                Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(2L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(2);
                Assert.assertTrue(rowChangedEvent.isTransactionCommit());
            }
        });
    }

    @Test
    public void testUpdateMultiRowsWithTransactionByBinlog() throws Exception {
        executeSql("INSERT INTO " + table + " values(1)");
        executeSql("INSERT INTO " + table + " values(2)");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                BinlogInfo latestBinlogInfo = getLatestBinlogInfo();
                executeSql("UPDATE " + table + " SET id=3");
                List<ChangedEvent> events = getEvents(4, SubscribeConstant.SEQ_FROM_BINLOGINFO, serverId,
                        latestBinlogInfo.getFile(), latestBinlogInfo.getPos(), -1, true);
                Assert.assertEquals(4, events.size());

                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertTrue(rowChangedEvent.isTransactionBegin());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());

                Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(1);
                Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(3L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(2);
                Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(3L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(2L, rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(3) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(3);
                Assert.assertTrue(rowChangedEvent.isTransactionCommit());
            }
        });
    }

    @Test
    public void testUpdateMultiRowsWithTransactionByTimestamp() throws Exception {
        executeSql("INSERT INTO " + table + " values(1)");
        executeSql("INSERT INTO " + table + " values(2)");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                long timestamp = new Date().getTime() / 1000;
                executeSql("UPDATE " + table + " SET id=3");
                waitForSync(5000);
                List<ChangedEvent> events = getEvents(4, SubscribeConstant.SEQ_FROM_TIMESTAMP, -1, null, -1, timestamp,
                        true);
                Assert.assertEquals(4, events.size());

                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertTrue(rowChangedEvent.isTransactionBegin());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());

                Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(1);
                Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(3L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(2);
                Assert.assertEquals(RowChangedEvent.UPDATE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertEquals(3L, rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(2L, rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(3) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(3);
                Assert.assertTrue(rowChangedEvent.isTransactionCommit());
            }
        });
    }

    @Test
    public void testDeleteNoTransactionByBinlog() throws Exception {
        executeSql("INSERT INTO " + table + " values(1)");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                BinlogInfo latestBinlogInfo = getLatestBinlogInfo();
                executeSql("DELETE FROM " + table + " WHERE id=1");
                List<ChangedEvent> events = getEvents(1, SubscribeConstant.SEQ_FROM_BINLOGINFO, serverId,
                        latestBinlogInfo.getFile(), latestBinlogInfo.getPos(), -1, false);
                Assert.assertEquals(1, events.size());
                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getOldValue());
            }
        });
    }

    @Test
    public void testDeleteNoTransactionByTimestamp() throws Exception {
        executeSql("INSERT INTO " + table + " values(1)");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                long timestamp = new Date().getTime() / 1000;
                executeSql("DELETE FROM " + table + " WHERE id=1");
                waitForSync(4000);
                List<ChangedEvent> events = getEvents(1, SubscribeConstant.SEQ_FROM_TIMESTAMP, -1, null, -1, timestamp,
                        false);
                Assert.assertEquals(1, events.size());
                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getOldValue());
            }
        });
    }

    @Test
    public void testDeleteWithTransactionByBinlog() throws Exception {
        executeSql("INSERT INTO " + table + " values(1)");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                BinlogInfo latestBinlogInfo = getLatestBinlogInfo();
                executeSql("DELETE FROM " + table + " WHERE id=1");
                List<ChangedEvent> events = getEvents(3, SubscribeConstant.SEQ_FROM_BINLOGINFO, serverId,
                        latestBinlogInfo.getFile(), latestBinlogInfo.getPos(), -1, true);
                Assert.assertEquals(3, events.size());

                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertTrue(rowChangedEvent.isTransactionBegin());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());

                Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(1);
                Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(2);
                Assert.assertTrue(rowChangedEvent.isTransactionCommit());
            }
        });
    }

    @Test
    public void testDeleteWithTransactionByTimestamp() throws Exception {
        executeSql("INSERT INTO " + table + " values(1)");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                long timestamp = new Date().getTime() / 1000;
                executeSql("DELETE FROM " + table + " WHERE id=1");
                waitForSync(4000);
                List<ChangedEvent> events = getEvents(3, SubscribeConstant.SEQ_FROM_TIMESTAMP, -1, null, -1, timestamp,
                        true);
                Assert.assertEquals(3, events.size());

                Assert.assertTrue(events.get(0) instanceof RowChangedEvent);
                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertTrue(rowChangedEvent.isTransactionBegin());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());

                Assert.assertTrue(events.get(1) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(1);
                Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getOldValue());

                Assert.assertTrue(events.get(2) instanceof RowChangedEvent);
                rowChangedEvent = (RowChangedEvent) events.get(2);
                Assert.assertTrue(rowChangedEvent.isTransactionCommit());
            }
        });
    }

    @Test
    public void testDeleteMultipleRowsWithTransactionByBinlog() throws Exception {
        executeSql("INSERT INTO " + table + " values(1)");
        executeSql("INSERT INTO " + table + " values(2)");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                BinlogInfo latestBinlogInfo = getLatestBinlogInfo();
                executeSql("DELETE FROM " + table);
                List<ChangedEvent> events = getEvents(4, SubscribeConstant.SEQ_FROM_BINLOGINFO, serverId,
                        latestBinlogInfo.getFile(), latestBinlogInfo.getPos(), -1, true);
                Assert.assertEquals(4, events.size());

                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertTrue(rowChangedEvent.isTransactionBegin());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());

                rowChangedEvent = (RowChangedEvent) events.get(1);
                Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getOldValue());

                rowChangedEvent = (RowChangedEvent) events.get(2);
                Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(2L, rowChangedEvent.getColumns().get("id").getOldValue());

                rowChangedEvent = (RowChangedEvent) events.get(3);
                Assert.assertTrue(rowChangedEvent.isTransactionCommit());
            }
        });
    }

    @Test
    public void testDeleteMultipleRowsWithTransactionByTimestamp() throws Exception {
        executeSql("INSERT INTO " + table + " values(1)");
        executeSql("INSERT INTO " + table + " values(2)");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                long timestamp = new Date().getTime() / 1000;
                executeSql("DELETE FROM " + table);
                waitForSync(5000);
                List<ChangedEvent> events = getEvents(4, SubscribeConstant.SEQ_FROM_TIMESTAMP, -1, null, -1, timestamp,
                        true);
                Assert.assertEquals(4, events.size());

                RowChangedEvent rowChangedEvent = (RowChangedEvent) events.get(0);
                Assert.assertTrue(rowChangedEvent.isTransactionBegin());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());

                rowChangedEvent = (RowChangedEvent) events.get(1);
                Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(1L, rowChangedEvent.getColumns().get("id").getOldValue());

                rowChangedEvent = (RowChangedEvent) events.get(2);
                Assert.assertEquals(RowChangedEvent.DELETE, rowChangedEvent.getActionType());
                Assert.assertEquals(table, rowChangedEvent.getTable());
                Assert.assertEquals(db, rowChangedEvent.getDatabase());
                Assert.assertEquals(1, rowChangedEvent.getColumns().size());
                Assert.assertNull(rowChangedEvent.getColumns().get("id").getNewValue());
                Assert.assertEquals(2L, rowChangedEvent.getColumns().get("id").getOldValue());

                rowChangedEvent = (RowChangedEvent) events.get(3);
                Assert.assertTrue(rowChangedEvent.isTransactionCommit());
            }
        });
    }

    @Test
    public void testDDlByBinlog() throws Exception {
        executeSql("DROP TABLE IF EXISTS DDLtest");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                BinlogInfo latestBinlogInfo = getLatestBinlogInfo();
                executeSql("CREATE TABLE DDLtest(id INT)");
                waitForSync(8000);
                List<ChangedEvent> events = getEvents(1, SubscribeConstant.SEQ_FROM_BINLOGINFO, serverId,
                        latestBinlogInfo.getFile(), latestBinlogInfo.getPos(), -1, false);
                Assert.assertEquals(1, events.size());
                Assert.assertTrue(events.get(0) instanceof DdlEvent);
                DdlEvent ddlEvent = (DdlEvent) events.get(0);
                Assert.assertEquals(db, ddlEvent.getDatabase());
                Assert.assertTrue("CREATE TABLE DDLtest(id INT)".equalsIgnoreCase(ddlEvent.getSql()));
                executeSql("DROP TABLE DDLtest");
            }
        });
    }
    
    @Test
    public void testDDlByTimestamp() throws Exception {
        executeSql("DROP TABLE IF EXISTS DDLtest");
        waitForSync(50);
        test(new TestLogic() {

            @Override
            public void doLogic() throws Exception {
                long timestamp = new Date().getTime()/ 1000;
                executeSql("CREATE TABLE DDLtest(id INT)");
                waitForSync(9000);
                List<ChangedEvent> events = getEvents(1, SubscribeConstant.SEQ_FROM_TIMESTAMP, -1,
                        null, -1, timestamp, false);
                Assert.assertEquals(1, events.size());
                Assert.assertTrue(events.get(0) instanceof DdlEvent);
                DdlEvent ddlEvent = (DdlEvent) events.get(0);
                Assert.assertEquals(db, ddlEvent.getDatabase());
                Assert.assertTrue("CREATE TABLE DDLtest(id INT)".equalsIgnoreCase(ddlEvent.getSql()));
                executeSql("DROP TABLE DDLtest");
            }
        });
    }

    public void doAfter() throws Exception {
        executeSql("DROP TABLE " + table);
    }
}
