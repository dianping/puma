/**
 * Project: puma-core
 * 
 * File Created at 2012-7-6
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
package com.dianping.puma.core.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.core.util.StreamUtils;

/**
 * TODO Comment of EventTransportUtilsTest
 * 
 * @author Leo Liang
 * 
 */
public class JsonEventCodecTest {

    private EventCodec codec = new JsonEventCodec();

    @Test
    public void testDdl() throws IOException {
        DdlEvent event = new DdlEvent();
        event.setDatabase("testdb");
        event.setExecuteTime(11111111);
        event.setTable("testtb");
        event.setSeq(1233);
        event.setSql("SELECT * FROM testtb");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = codec.encode(event);
        bos.write(intToByteArray(data.length));
        bos.write(data);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        DdlEvent result = (DdlEvent) readEvent(bis);
        Assert.assertEquals(event.getDatabase(), result.getDatabase());
        Assert.assertEquals(event.getExecuteTime(), result.getExecuteTime());
        Assert.assertEquals(event.getSeq(), result.getSeq());
        Assert.assertEquals(event.getSql(), result.getSql());
        Assert.assertEquals(event.getTable(), result.getTable());
    }

    @Test
    public void testDml() throws IOException {
        RowChangedEvent event = new RowChangedEvent();
        event.setDatabase("testdb");
        event.setExecuteTime(11111111);
        event.setTable("testtb");
        event.setSeq(1233);
        event.setActionType(RowChangedEvent.INSERT);
        event.setTransactionBegin(false);
        event.setTransactionCommit(false);
        Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();
        columns.put("a", new ColumnInfo(false, "1", "2"));
        event.setColumns(columns);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = codec.encode(event);
        bos.write(intToByteArray(data.length));
        bos.write(data);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        RowChangedEvent result = (RowChangedEvent) readEvent(bis);
        Assert.assertEquals(event.getDatabase(), result.getDatabase());
        Assert.assertEquals(event.getExecuteTime(), result.getExecuteTime());
        Assert.assertEquals(event.getSeq(), result.getSeq());
        Assert.assertEquals(event.getActionType(), result.getActionType());
        Assert.assertEquals(event.getTable(), result.getTable());
        Assert.assertEquals(event.isTransactionBegin(), result.isTransactionBegin());
        Assert.assertEquals(event.isTransactionCommit(), result.isTransactionCommit());
        for (Map.Entry<String, ColumnInfo> entry : event.getColumns().entrySet()) {
            Assert.assertEquals(entry.getValue().isKey(), result.getColumns().get(entry.getKey()).isKey());
            Assert.assertEquals(entry.getValue().getNewValue(), result.getColumns().get(entry.getKey()).getNewValue());
            Assert.assertEquals(entry.getValue().getOldValue(), result.getColumns().get(entry.getKey()).getOldValue());
        }
    }

    @Test
    public void testDmlNullKey() throws IOException {
        RowChangedEvent event = new RowChangedEvent();
        event.setDatabase("testdb");
        event.setExecuteTime(11111111);
        event.setTable("testtb");
        event.setSeq(1233);
        event.setActionType(RowChangedEvent.INSERT);
        event.setTransactionBegin(false);
        event.setTransactionCommit(false);
        Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();
        columns.put(null, new ColumnInfo(false, null, null));
        event.setColumns(columns);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = codec.encode(event);
        bos.write(intToByteArray(data.length));
        bos.write(data);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        RowChangedEvent result = (RowChangedEvent) readEvent(bis);
        Assert.assertEquals(event.getDatabase(), result.getDatabase());
        Assert.assertEquals(event.getExecuteTime(), result.getExecuteTime());
        Assert.assertEquals(event.getSeq(), result.getSeq());
        Assert.assertEquals(event.getActionType(), result.getActionType());
        Assert.assertEquals(event.getTable(), result.getTable());
        Assert.assertEquals(event.isTransactionBegin(), result.isTransactionBegin());
        Assert.assertEquals(event.isTransactionCommit(), result.isTransactionCommit());
        for (Map.Entry<String, ColumnInfo> entry : event.getColumns().entrySet()) {
            Assert.assertEquals(entry.getValue().isKey(), result.getColumns().get("[NullKey]").isKey());
            Assert.assertNull(result.getColumns().get("[NullKey]").getNewValue());
            Assert.assertNull(result.getColumns().get("[NullKey]").getOldValue());
        }
    }

    @Test
    public void testDmlNullValue() throws IOException {
        RowChangedEvent event = new RowChangedEvent();
        event.setDatabase("testdb");
        event.setExecuteTime(11111111);
        event.setTable("testtb");
        event.setSeq(1233);
        event.setActionType(RowChangedEvent.INSERT);
        event.setTransactionBegin(false);
        event.setTransactionCommit(false);
        Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();
        columns.put("dd", new ColumnInfo(false, null, null));
        event.setColumns(columns);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = codec.encode(event);
        bos.write(intToByteArray(data.length));
        bos.write(data);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        RowChangedEvent result = (RowChangedEvent) readEvent(bis);
        Assert.assertEquals(event.getDatabase(), result.getDatabase());
        Assert.assertEquals(event.getExecuteTime(), result.getExecuteTime());
        Assert.assertEquals(event.getSeq(), result.getSeq());
        Assert.assertEquals(event.getActionType(), result.getActionType());
        Assert.assertEquals(event.getTable(), result.getTable());
        Assert.assertEquals(event.isTransactionBegin(), result.isTransactionBegin());
        Assert.assertEquals(event.isTransactionCommit(), result.isTransactionCommit());
        for (Map.Entry<String, ColumnInfo> entry : event.getColumns().entrySet()) {
            Assert.assertEquals(entry.getValue().isKey(), result.getColumns().get("dd").isKey());
            Assert.assertNull(result.getColumns().get("dd").getNewValue());
            Assert.assertNull(result.getColumns().get("dd").getOldValue());
        }
    }

    private Event readEvent(InputStream is) throws IOException {
        byte[] lengthArray = new byte[4];
        StreamUtils.readFully(is, lengthArray, 0, 4);
        int length = ByteArrayUtils.byteArrayToInt(lengthArray, 0, 4);
        byte[] data = new byte[length];
        StreamUtils.readFully(is, data, 0, length);
        return codec.decode(data);
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }

}
