package com.dianping.puma.core.codec;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.constant.DdlEventSubType;
import com.dianping.puma.core.util.constant.DdlEventType;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.core.util.sql.DMLType;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RawEventCodecTest {

    private RawEventCodec codec = new RawEventCodec();

    private long now = System.currentTimeMillis();

    private byte[] values = new byte[]{0, 1, 2, 3, 4};

    @Test
    public void testEncodeDecodeList() throws IOException {
        List<Event> eventList = new ArrayList<Event>();

        for (int k = 0; k < 3; k++) {
            DdlEvent event = new DdlEvent();
            // base info
            setChangedEventProperty(event);

            // ddl event info
            event.setSql("SELECT * FROM testtb");
            event.setDDLType(DDLType.ALTER_DATABASE);
            event.setDdlEventSubType(DdlEventSubType.DDL_ALTER_DATABASE);
            event.setDdlEventType(DdlEventType.DDL_ALTER);

            eventList.add(event);
        }

        byte[] data = codec.encodeList(eventList);

        List<Event> result = codec.decodeList(data);

        Assert.assertEquals(result.size(),eventList.size());
    }

    @Test
    public void testEncodeDdlEvent() throws IOException {
        DdlEvent event = new DdlEvent();
        // base info
        setChangedEventProperty(event);

        // ddl event info
        event.setSql("SELECT * FROM testtb");
        event.setDDLType(DDLType.ALTER_DATABASE);
        event.setDdlEventSubType(DdlEventSubType.DDL_ALTER_DATABASE);
        event.setDdlEventType(DdlEventType.DDL_ALTER);

        byte[] encode = codec.encode(event);

        System.out.println("raw length = " + encode.length);

        DdlEvent result = (DdlEvent) codec.decode(encode);

        assertChangedEventProperty(result);

        Assert.assertEquals("SELECT * FROM testtb", result.getSql());
    }

    private void assertChangedEventProperty(ChangedEvent result) {
        Assert.assertEquals("testdb", result.getDatabase());
        Assert.assertEquals(11111111, result.getExecuteTime());
        Assert.assertEquals("testtb", result.getTable());
        Assert.assertEquals(1233, result.getSeq());
        Assert.assertEquals("mysql-bin.000000", result.getBinlogInfo().getBinlogFile());
        Assert.assertEquals(4, result.getBinlogInfo().getBinlogPosition());
        Assert.assertEquals(2, result.getBinlogInfo().getEventIndex());
        Assert.assertEquals(123123L, result.getServerId());
        Assert.assertEquals(123124L, result.getBinlogInfo().getServerId());
    }

    private void setChangedEventProperty(ChangedEvent event) {
        event.setDatabase("testdb");
        event.setExecuteTime(11111111);
        event.setTable("testtb");
        event.setSeq(1233);
        event.setBinlogInfo(new BinlogInfo(123124L, "mysql-bin.000000", 4L, 2, 0));
        event.setServerId(123123L);
    }

    @Test
    public void testEncodeRowChangedEventInsert() throws IOException {
        RowChangedEvent event = new RowChangedEvent();
        // base info
        setChangedEventProperty(event);

        event.setDmlType(DMLType.INSERT);
        event.setTransactionBegin(true);
        event.setTransactionCommit(false);

        Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();

        event.setColumns(columns);

        putColumnInfos(columns, false, true);

        long start = System.nanoTime();
        byte[] encode = codec.encode(event);
        long end = System.nanoTime();

        long start1 = System.nanoTime();
        long end1 = System.nanoTime();

        long start2 = System.nanoTime();
        RowChangedEvent result = (RowChangedEvent) codec.decode(encode);
        long end2 = System.nanoTime();

        System.out.println("raw length = " + encode.length + " encode time = " + (end - start) / 1000 + " decode time = " + (end2 - start2) / 1000);

        assertChangedEventProperty(result);

        // row changed event base info
        Assert.assertEquals(DMLType.INSERT, result.getDmlType());
        Assert.assertEquals(true, result.isTransactionBegin());
        Assert.assertEquals(false, result.isTransactionCommit());
        Assert.assertEquals(22, result.getColumns().size());

        // row changed event column info

        assertRowChangedEventColumn(result, false, true);
    }

    @Test
    public void testEncodeRowChangedEventDelete() throws IOException {
        RowChangedEvent event = new RowChangedEvent();
        // base info
        setChangedEventProperty(event);

        event.setDmlType(DMLType.DELETE);
        event.setTransactionBegin(true);
        event.setTransactionCommit(false);

        Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();

        event.setColumns(columns);

        putColumnInfos(columns, true, false);

        long start = System.nanoTime();
        byte[] encode = codec.encode(event);
        long end = System.nanoTime();

        long start1 = System.nanoTime();
        long end1 = System.nanoTime();

        long start2 = System.nanoTime();
        RowChangedEvent result = (RowChangedEvent) codec.decode(encode);
        long end2 = System.nanoTime();

        System.out.println("raw length = " + encode.length + " encode time = " + (end - start) / 1000 + " decode time = " + (end2 - start2) / 1000);

        assertChangedEventProperty(result);

        // row changed event base info
        Assert.assertEquals(DMLType.DELETE, result.getDmlType());
        Assert.assertEquals(true, result.isTransactionBegin());
        Assert.assertEquals(false, result.isTransactionCommit());
        Assert.assertEquals(22, result.getColumns().size());

        // row changed event column info

        assertRowChangedEventColumn(result, true, false);
    }

    @Test
    public void testEncodeRowChangedEventUpdate() throws IOException {
        RowChangedEvent event = new RowChangedEvent();
        // base info
        setChangedEventProperty(event);

        event.setDmlType(DMLType.UPDATE);
        event.setTransactionBegin(true);
        event.setTransactionCommit(false);

        Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();

        event.setColumns(columns);

        putColumnInfos(columns, true, true);

        long start = System.nanoTime();
        byte[] encode = codec.encode(event);
        long end = System.nanoTime();

        long start1 = System.nanoTime();
        long end1 = System.nanoTime();

        long start2 = System.nanoTime();
        RowChangedEvent result = (RowChangedEvent) codec.decode(encode);
        long end2 = System.nanoTime();

        System.out.println("raw length = " + encode.length + " encode time = " + (end - start) / 1000 + " decode time = " + (end2 - start2) / 1000);

        assertChangedEventProperty(result);

        // row changed event base info
        Assert.assertEquals(DMLType.UPDATE, result.getDmlType());
        Assert.assertEquals(true, result.isTransactionBegin());
        Assert.assertEquals(false, result.isTransactionCommit());
        Assert.assertEquals(22, result.getColumns().size());

        // row changed event column info

        assertRowChangedEventColumn(result, true, true);
    }

    private void assertRowChangedEventColumn(RowChangedEvent result, boolean checkOld, boolean checkNew) {
        if (checkOld) {
            byte[] btc = (byte[]) result.getColumns().get("bit").getOldValue();
            for (int i = 0; i < values.length; i++) {
                Assert.assertEquals(values[i], btc[i]);
            }

            byte[] bbc = (byte[]) result.getColumns().get("blob").getOldValue();
            for (int i = 0; i < values.length; i++) {
                Assert.assertEquals(values[i], bbc[i]);
            }

            Date dtc = (Date) result.getColumns().get("date").getOldValue();
            Assert.assertEquals(now, dtc.getTime());

            String dt2c = (String) result.getColumns().get("datetime2").getOldValue();
            Assert.assertEquals("2015-5-1 12:00:12", dt2c);

            String datc = (String) result.getColumns().get("datetime").getOldValue();
            Assert.assertEquals("2015-5-2 12:00:12", datc);

            BigDecimal decc = (BigDecimal) result.getColumns().get("decimal").getOldValue();
            Assert.assertEquals(new BigDecimal(123213), decc);

            Double dbc = (Double) result.getColumns().get("double").getOldValue();
            Assert.assertEquals(12321.222212321312312, dbc);

            Integer enc = (Integer) result.getColumns().get("enum").getOldValue();
            Assert.assertEquals(new Integer(1), enc);

            Float flc = (Float) result.getColumns().get("float").getOldValue();
            Assert.assertEquals(123.1f, flc);

            Integer int24c = (Integer) result.getColumns().get("int24").getOldValue();
            Assert.assertEquals(new Integer(123), int24c);

            Integer intc = (Integer) result.getColumns().get("int").getOldValue();
            Assert.assertEquals(new Integer(-321), intc);

            Long llc = (Long) result.getColumns().get("longlong").getOldValue();
            Assert.assertEquals(new Long(12312312L), llc);

            Object nuc = result.getColumns().get("null").getOldValue();
            Assert.assertEquals(null, nuc);

            Long setc = (Long) result.getColumns().get("set").getOldValue();
            Assert.assertEquals(new Long(123L), setc);

            Integer stc = (Integer) result.getColumns().get("short").getOldValue();
            Assert.assertEquals(new Integer(3), stc);

            String strc = (String) result.getColumns().get("string").getOldValue();
            Assert.assertEquals("test sql is test", strc);

            String tm2c = (String) result.getColumns().get("time2").getOldValue();
            Assert.assertEquals("2015-6-4 12:11:3", tm2c);

            Time tmc = (Time) result.getColumns().get("time").getOldValue();
            Assert.assertEquals(now, tmc.getTime());

            String tms2c = (String) result.getColumns().get("timestamp2").getOldValue();
            Assert.assertEquals("2015-7-1 12:23:4", tms2c);

            Timestamp tmsc = (Timestamp) result.getColumns().get("timestamp").getOldValue();
            Assert.assertEquals(new Timestamp(now), tmsc);

            Integer tyc = (Integer) result.getColumns().get("tiny").getOldValue();
            Assert.assertEquals(new Integer(10), tyc);

            Short yec = (Short) result.getColumns().get("year").getOldValue();
            Assert.assertEquals(new Short((short) 2012), yec);
        }
        if (checkNew) {
            byte[] btc = (byte[]) result.getColumns().get("bit").getNewValue();
            for (int i = 0; i < values.length; i++) {
                Assert.assertEquals(values[i], btc[i]);
            }

            byte[] bbc = (byte[]) result.getColumns().get("blob").getNewValue();
            for (int i = 0; i < values.length; i++) {
                Assert.assertEquals(values[i], bbc[i]);
            }

            Date dtc = (Date) result.getColumns().get("date").getNewValue();
            Assert.assertEquals(now, dtc.getTime());

            String dt2c = (String) result.getColumns().get("datetime2").getNewValue();
            Assert.assertEquals("2015-5-1 12:00:12", dt2c);

            String datc = (String) result.getColumns().get("datetime").getNewValue();
            Assert.assertEquals("2015-5-2 12:00:12", datc);

            BigDecimal decc = (BigDecimal) result.getColumns().get("decimal").getNewValue();
            Assert.assertEquals(new BigDecimal(123213), decc);

            Double dbc = (Double) result.getColumns().get("double").getNewValue();
            Assert.assertEquals(12321.222212321312312, dbc);

            Integer enc = (Integer) result.getColumns().get("enum").getNewValue();
            Assert.assertEquals(new Integer(1), enc);

            Float flc = (Float) result.getColumns().get("float").getNewValue();
            Assert.assertEquals(123.1f, flc);

            Integer int24c = (Integer) result.getColumns().get("int24").getNewValue();
            Assert.assertEquals(new Integer(123), int24c);

            Integer intc = (Integer) result.getColumns().get("int").getNewValue();
            Assert.assertEquals(new Integer(-321), intc);

            Long llc = (Long) result.getColumns().get("longlong").getNewValue();
            Assert.assertEquals(new Long(12312312L), llc);

            Object nuc = result.getColumns().get("null").getNewValue();
            Assert.assertEquals(null, nuc);

            Long setc = (Long) result.getColumns().get("set").getNewValue();
            Assert.assertEquals(new Long(123L), setc);

            Integer stc = (Integer) result.getColumns().get("short").getNewValue();
            Assert.assertEquals(new Integer(3), stc);

            String strc = (String) result.getColumns().get("string").getNewValue();
            Assert.assertEquals("test sql is test", strc);

            String tm2c = (String) result.getColumns().get("time2").getNewValue();
            Assert.assertEquals("2015-6-4 12:11:3", tm2c);

            Time tmc = (Time) result.getColumns().get("time").getNewValue();
            Assert.assertEquals(now, tmc.getTime());

            String tms2c = (String) result.getColumns().get("timestamp2").getNewValue();
            Assert.assertEquals("2015-7-1 12:23:4", tms2c);

            Timestamp tmsc = (Timestamp) result.getColumns().get("timestamp").getNewValue();
            Assert.assertEquals(new Timestamp(now), tmsc);

            Integer tyc = (Integer) result.getColumns().get("tiny").getNewValue();
            Assert.assertEquals(new Integer(10), tyc);

            Short yec = (Short) result.getColumns().get("year").getNewValue();
            Assert.assertEquals(new Short((short) 2012), yec);
        }
    }

    private void putColumnInfos(Map<String, ColumnInfo> columns, boolean useOld, boolean useNew) {
        putColumnInfo(columns, "bit", false, useOld ? values : null, useNew ? values : null);
        putColumnInfo(columns, "blob", false, useOld ? values : null, useNew ? values : null);
        putColumnInfo(columns, "date", false, useOld ? new Date(now) : null, useNew ? new Date(now) : null);
        putColumnInfo(columns, "datetime2", false, useOld ? "2015-5-1 12:00:12" : null, useNew ? "2015-5-1 12:00:12"
                : null);
        putColumnInfo(columns, "datetime", false, useOld ? "2015-5-2 12:00:12" : null, useNew ? "2015-5-2 12:00:12"
                : null);
        putColumnInfo(columns, "decimal", false, useOld ? new BigDecimal(123213) : null, useNew ? new BigDecimal(123213)
                : null);
        putColumnInfo(columns, "double", false, useOld ? 12321.222212321312312 : null, useNew ? 12321.222212321312312
                : null);
        putColumnInfo(columns, "enum", false, useOld ? new Integer(1) : null, useNew ? new Integer(1) : null);
        putColumnInfo(columns, "float", false, useOld ? new Float(123.1f) : null, useNew ? new Float(123.1f) : null);
        putColumnInfo(columns, "int24", false, useOld ? new Integer(123) : null, useNew ? new Integer(123) : null);
        putColumnInfo(columns, "int", false, useOld ? new Integer(-321) : null, useNew ? new Integer(-321) : null);
        putColumnInfo(columns, "longlong", false, useOld ? new Long(12312312L) : null, useNew ? new Long(12312312L)
                : null);
        putColumnInfo(columns, "null", false, useOld ? null : null, useNew ? null : null);
        putColumnInfo(columns, "set", false, useOld ? new Long(123L) : null, useNew ? new Long(123L) : null);
        putColumnInfo(columns, "short", false, useOld ? new Integer(3) : null, useNew ? new Integer(3) : null);
        putColumnInfo(columns, "string", false, useOld ? "test sql is test" : null, useNew ? "test sql is test" : null);
        putColumnInfo(columns, "time2", false, useOld ? "2015-6-4 12:11:3" : null, useNew ? "2015-6-4 12:11:3" : null);
        putColumnInfo(columns, "time", false, useOld ? new Time(now) : null, useNew ? new Time(now) : null);
        putColumnInfo(columns, "timestamp2", false, useOld ? "2015-7-1 12:23:4" : null, useNew ? "2015-7-1 12:23:4"
                : null);
        putColumnInfo(columns, "timestamp", false, useOld ? new Timestamp(now) : null, useNew ? new Timestamp(now) : null);
        putColumnInfo(columns, "tiny", false, useOld ? new Integer(10) : null, useNew ? new Integer(10) : null);
        putColumnInfo(columns, "year", false, useOld ? new Short((short) 2012) : null, useNew ? new Short((short) 2012)
                : null);
    }

    private void putColumnInfo(Map<String, ColumnInfo> columns, String columnName, boolean isKey, Object oldValue,
                               Object newValue) {
        ColumnInfo columnInfo = new ColumnInfo(false, oldValue, newValue);
        columns.put(columnName, columnInfo);
    }
}
