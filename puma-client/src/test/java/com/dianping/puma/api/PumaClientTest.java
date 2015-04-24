package com.dianping.puma.api;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.core.util.PumaThreadUtils;

public class PumaClientTest {
    @Test
    public void testNoConf() {
        try {
            new PumaClient(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {

        }

    }

    @Test
    public void testConstruct() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.codecType("json");
        builder.ddl(true);
        builder.dml(false);
        builder.host("111.3.3");
        builder.name("test");
        builder.port(123);
        builder.seqFileBase("/tmp/11111");
        builder.tables("cat", "a", "b*");
        builder.tables("me", "d");
        builder.target("fff");
        builder.transaction(true);
        builder.binlog("fff");
        builder.binlogPos(4);
        builder.serverId(1111);
        PumaClient pumaClient = new PumaClient(builder.build());
        Assert.assertNotNull(getValue(pumaClient, "config"));
        Assert.assertNotNull(getValue(pumaClient, "sequenceHolder"));
        Assert.assertNotNull(getValue(pumaClient, "codec"));
        //Assert.assertEquals(false, getValue(pumaClient, "active"));
    }

    @Test
    public void testUrl() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.codecType("json");
        builder.ddl(true);
        builder.dml(false);
        builder.host("111.3.3");
        builder.name("test");
        builder.port(123);
        builder.seqFileBase("/tmp/11111");
        builder.tables("cat", "a", "b*");
        builder.tables("me", "d");
        builder.target("fff");
        builder.transaction(true);
        builder.binlog("fff");
        builder.binlogPos(4);
        builder.serverId(1111);
        Configuration conf = builder.build();
        Assert.assertEquals("http://111.3.3:123/puma/channel", conf.buildUrl());
    }

    @Test
    public void testParamWithoutBinlogInfo() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.codecType("json");
        builder.ddl(true);
        builder.dml(false);
        builder.host("111.3.3");
        builder.name("test");
        builder.port(123);
        builder.seqFileBase("/tmp/11111");
        builder.tables("cat", "a", "b*");
        builder.tables("me", "d");
        builder.target("fff");
        builder.transaction(true);
        builder.binlog("fff");
        builder.binlogPos(4);
        builder.serverId(1111);
        Configuration conf = builder.build();
        Assert.assertEquals("seq=-1&name=test&target=fff&ddl=true&dml=false&ts=true&codec=json&dt=cat.a&dt=cat.b*&dt=me.d",
                conf.buildRequestParamString(-1));
    }

    @Test
    public void testParamWithoutBinlogInfo2() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.codecType("json");
        builder.ddl(true);
        builder.dml(false);
        builder.host("111.3.3");
        builder.name("test");
        builder.port(123);
        builder.seqFileBase("/tmp/11111");
        builder.tables("cat", "a", "b*");
        builder.tables("me", "d");
        builder.target("fff");
        builder.transaction(true);
        builder.binlog("fff");
        builder.binlogPos(4);
        builder.serverId(1111);
        Configuration conf = builder.build();
        Assert.assertEquals("seq=-2&name=test&target=fff&ddl=true&dml=false&ts=true&codec=json&dt=cat.a&dt=cat.b*&dt=me.d",
                conf.buildRequestParamString(-2));
    }

    @Test
    public void testParamWithoutBinlogInfo3() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.codecType("json");
        builder.ddl(true);
        builder.dml(false);
        builder.host("111.3.3");
        builder.name("test");
        builder.port(123);
        builder.seqFileBase("/tmp/11111");
        builder.tables("cat", "a", "b*");
        builder.tables("me", "d");
        builder.target("fff");
        builder.transaction(true);
        builder.binlog("fff");
        builder.binlogPos(4);
        builder.serverId(1111);
        Configuration conf = builder.build();
        Assert.assertEquals("seq=10&name=test&target=fff&ddl=true&dml=false&ts=true&codec=json&dt=cat.a&dt=cat.b*&dt=me.d",
                conf.buildRequestParamString(10));
    }

    @Test
    public void testParamWithBinlogInfo() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.codecType("json");
        builder.ddl(true);
        builder.dml(false);
        builder.host("111.3.3");
        builder.name("test");
        builder.port(123);
        builder.seqFileBase("/tmp/11111");
        builder.tables("cat", "a", "b*");
        builder.tables("me", "d");
        builder.target("fff");
        builder.transaction(true);
        builder.binlog("fff");
        builder.binlogPos(4);
        builder.serverId(1111);
        Configuration conf = builder.build();
        Assert.assertEquals(
                "seq=-3&binlog=fff&binlogPos=4&serverId=1111&name=test&target=fff&ddl=true&dml=false&ts=true&codec=json&dt=cat.a&dt=cat.b*&dt=me.d",
                conf.buildRequestParamString(-3));
    }

    @Test
    public void testParamWithTimeStamp() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.codecType("json");
        builder.ddl(true);
        builder.dml(false);
        builder.host("111.3.3");
        builder.name("test");
        builder.port(123);
        builder.seqFileBase("/tmp/11111");
        builder.tables("cat", "a", "b*");
        builder.tables("me", "d");
        builder.target("fff");
        builder.transaction(true);
        builder.timeStamp(12345);
        Configuration conf = builder.build();
        Assert.assertEquals(
                "seq=-4&timestamp=12345&name=test&target=fff&ddl=true&dml=false&ts=true&codec=json&dt=cat.a&dt=cat.b*&dt=me.d",
                conf.buildRequestParamString(-4));
    }

    @Test
    public void testReconnect() {

    }

    @Test
    public void testApi() throws InterruptedException {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.codecType("json");
        configBuilder.ddl(true);
        configBuilder.dml(false);
        configBuilder.host("localhost");
        configBuilder.name("test");
        configBuilder.port(7862);
        configBuilder.tables("test1", "a", "b*");
        configBuilder.tables("test2", "d");
        configBuilder.target("fff");
        configBuilder.transaction(true);
        configBuilder.binlog("fff");
        configBuilder.binlogPos(4);
        configBuilder.serverId(1111);

        List<Event> eventsSent = new ArrayList<Event>();

        DdlEvent event1 = new DdlEvent();
        event1.setBinlog("dddd");
        event1.setBinlogPos(111);
        event1.setDatabase("test1");
        event1.setExecuteTime(1111122233);
        event1.setServerId(11111);
        event1.setSeq(111111222223334L);
        event1.setSql("CREATE TABLE dddd");
        eventsSent.add(event1);

        RowChangedEvent event2 = new RowChangedEvent();
        event2.setBinlog("dddd");
        event2.setBinlogPos(111);
        event2.setDatabase("test1");
        event2.setExecuteTime(1111122233);
        event2.setServerId(11111);
        event2.setSeq(111111222223334L);
        event2.setActionType(RowChangedEvent.INSERT);
        event2.setTable("a");
        event2.setTransactionBegin(false);
        event2.setTransactionCommit(false);
        Map<String, ColumnInfo> columns = new HashMap<String, RowChangedEvent.ColumnInfo>();
        columns.put("id", new ColumnInfo(false, null, 2));
        event2.setColumns(columns);
        eventsSent.add(event2);

        RowChangedEvent event3 = new RowChangedEvent();
        event3.setBinlog("dddd");
        event3.setBinlogPos(111);
        event3.setDatabase("test1");
        event3.setExecuteTime(1111122233);
        event3.setServerId(11111);
        event3.setSeq(111111222223334L);
        event3.setActionType(RowChangedEvent.UPDATE);
        event3.setTable("a");
        event3.setTransactionBegin(false);
        event3.setTransactionCommit(false);
        Map<String, ColumnInfo> columns3 = new HashMap<String, RowChangedEvent.ColumnInfo>();
        columns3.put("id", new ColumnInfo(false, 1, 2));
        event3.setColumns(columns3);
        eventsSent.add(event3);

        RowChangedEvent event4 = new RowChangedEvent();
        event4.setBinlog("dddd");
        event4.setBinlogPos(111);
        event4.setDatabase("test1");
        event4.setExecuteTime(1111122233);
        event4.setServerId(11111);
        event4.setSeq(111111222223334L);
        event4.setActionType(RowChangedEvent.DELETE);
        event4.setTable("a");
        event4.setTransactionBegin(false);
        event4.setTransactionCommit(false);
        Map<String, ColumnInfo> columns4 = new HashMap<String, RowChangedEvent.ColumnInfo>();
        columns4.put("id", new ColumnInfo(false, 1, null));
        event4.setColumns(columns4);
        eventsSent.add(event4);

        RowChangedEvent event5 = new RowChangedEvent();
        event5.setBinlog("dddd");
        event5.setBinlogPos(111);
        event5.setDatabase("test1");
        event5.setExecuteTime(1111122233);
        event5.setServerId(11111);
        event5.setSeq(111111222223334L);
        event5.setTransactionBegin(true);
        eventsSent.add(event5);

        RowChangedEvent event6 = new RowChangedEvent();
        event6.setBinlog("dddd");
        event6.setBinlogPos(111);
        event6.setDatabase("test1");
        event6.setExecuteTime(1111122233);
        event6.setServerId(11111);
        event6.setSeq(111111222223334L);
        event6.setTransactionCommit(true);
        eventsSent.add(event6);

        final List<Event> eventsReceived = new ArrayList<Event>();

        StartMockPumaServer(eventsSent);

        PumaClient client = new PumaClient(configBuilder.build());

        client.register(new EventListener() {

            @Override
            public void onSkipEvent(Event event) {

            }

            @Override
            public boolean onException(Event event, Exception e) {
                return true;
            }

            @Override
            public void onEvent(Event event) throws Exception {
                eventsReceived.add(event);
            }

            @Override
            public void onConnectException(Exception e) {
            }

            @Override
            public void onConnected() {

            }
            @Override
            public void onHeartbeatEvent(Event event){
            	
            }
        });

        client.start();

        Thread.sleep(5 * 1000);

        Assert.assertArrayEquals(eventsSent.toArray(new ChangedEvent[0]), eventsReceived.toArray(new ChangedEvent[0]));
    }

    @Test
    public void testApiException() throws InterruptedException {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.codecType("json");
        configBuilder.ddl(true);
        configBuilder.dml(false);
        configBuilder.host("localhost");
        configBuilder.name("test");
        configBuilder.port(7862);
        configBuilder.tables("test1", "a", "b*");
        configBuilder.tables("test2", "d");
        configBuilder.target("fff");
        configBuilder.transaction(true);
        configBuilder.binlog("fff");
        configBuilder.binlogPos(4);
        configBuilder.serverId(1111);

        List<Event> eventsSent = new ArrayList<Event>();

        DdlEvent event1 = new DdlEvent();
        event1.setBinlog("dddd");
        event1.setBinlogPos(111);
        event1.setDatabase("test1");
        event1.setExecuteTime(1111122233);
        event1.setServerId(11111);
        event1.setSeq(111111222223334L);
        event1.setSql("CREATE TABLE dddd");
        eventsSent.add(event1);

        RowChangedEvent event2 = new RowChangedEvent();
        event2.setBinlog("dddd");
        event2.setBinlogPos(111);
        event2.setDatabase("test1");
        event2.setExecuteTime(1111122233);
        event2.setServerId(11111);
        event2.setSeq(111111222223334L);
        event2.setActionType(RowChangedEvent.INSERT);
        event2.setTable("a");
        event2.setTransactionBegin(false);
        event2.setTransactionCommit(false);
        Map<String, ColumnInfo> columns = new HashMap<String, RowChangedEvent.ColumnInfo>();
        columns.put("id", new ColumnInfo(false, null, 2));
        event2.setColumns(columns);
        eventsSent.add(event2);

        RowChangedEvent event3 = new RowChangedEvent();
        event3.setBinlog("dddd");
        event3.setBinlogPos(111);
        event3.setDatabase("test1");
        event3.setExecuteTime(1111122233);
        event3.setServerId(11111);
        event3.setSeq(111111222223334L);
        event3.setActionType(RowChangedEvent.UPDATE);
        event3.setTable("a");
        event3.setTransactionBegin(false);
        event3.setTransactionCommit(false);
        Map<String, ColumnInfo> columns3 = new HashMap<String, RowChangedEvent.ColumnInfo>();
        columns3.put("id", new ColumnInfo(false, 1, 2));
        event3.setColumns(columns3);
        eventsSent.add(event3);

        RowChangedEvent event4 = new RowChangedEvent();
        event4.setBinlog("dddd");
        event4.setBinlogPos(111);
        event4.setDatabase("test1");
        event4.setExecuteTime(1111122233);
        event4.setServerId(11111);
        event4.setSeq(111111222223334L);
        event4.setActionType(RowChangedEvent.DELETE);
        event4.setTable("a");
        event4.setTransactionBegin(false);
        event4.setTransactionCommit(false);
        Map<String, ColumnInfo> columns4 = new HashMap<String, RowChangedEvent.ColumnInfo>();
        columns4.put("id", new ColumnInfo(false, 1, null));
        event4.setColumns(columns4);
        eventsSent.add(event4);

        RowChangedEvent event5 = new RowChangedEvent();
        event5.setBinlog("dddd");
        event5.setBinlogPos(111);
        event5.setDatabase("test1");
        event5.setExecuteTime(1111122233);
        event5.setServerId(11111);
        event5.setSeq(111111222223334L);
        event5.setTransactionBegin(true);
        eventsSent.add(event5);

        RowChangedEvent event6 = new RowChangedEvent();
        event6.setBinlog("dddd");
        event6.setBinlogPos(111);
        event6.setDatabase("test1");
        event6.setExecuteTime(1111122233);
        event6.setServerId(11111);
        event6.setSeq(111111222223334L);
        event6.setTransactionCommit(true);
        eventsSent.add(event6);

        final List<Event> eventsReceived = new ArrayList<Event>();

        StartMockPumaServer(eventsSent);

        PumaClient client = new PumaClient(configBuilder.build());

        client.register(new EventListener() {
            private int i = 0;

            @Override
            public void onSkipEvent(Event event) {

            }

            @Override
            public boolean onException(Event event, Exception e) {
                return false;
            }

            @Override
            public void onEvent(Event event) throws Exception {
                if (++i == 2) {
                    throw new Exception();
                }
                eventsReceived.add(event);
            }

            @Override
            public void onConnectException(Exception e) {
            }

            @Override
            public void onConnected() {

            }
            @Override
            public void onHeartbeatEvent(Event event){
            	
            }
        });

        client.start();

        Thread.sleep(5 * 1000);

        Assert.assertArrayEquals(eventsSent.toArray(new ChangedEvent[0]), eventsReceived.toArray(new ChangedEvent[0]));
    }

    @Test
    public void testApiSkip() throws InterruptedException {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.codecType("json");
        configBuilder.ddl(true);
        configBuilder.dml(false);
        configBuilder.host("localhost");
        configBuilder.name("test");
        configBuilder.port(7862);
        configBuilder.tables("test1", "a", "b*");
        configBuilder.tables("test2", "d");
        configBuilder.target("fff");
        configBuilder.transaction(true);
        configBuilder.binlog("fff");
        configBuilder.binlogPos(4);
        configBuilder.serverId(1111);

        ArrayList<Event> eventsSent = new ArrayList<Event>();

        DdlEvent event1 = new DdlEvent();
        event1.setBinlog("dddd");
        event1.setBinlogPos(111);
        event1.setDatabase("test1");
        event1.setExecuteTime(1111122233);
        event1.setServerId(11111);
        event1.setSeq(111111222223334L);
        event1.setSql("CREATE TABLE dddd");
        eventsSent.add(event1);

        RowChangedEvent event2 = new RowChangedEvent();
        event2.setBinlog("dddd");
        event2.setBinlogPos(111);
        event2.setDatabase("test1");
        event2.setExecuteTime(1111122233);
        event2.setServerId(11111);
        event2.setSeq(111111222223334L);
        event2.setActionType(RowChangedEvent.INSERT);
        event2.setTable("a");
        event2.setTransactionBegin(false);
        event2.setTransactionCommit(false);
        Map<String, ColumnInfo> columns = new HashMap<String, RowChangedEvent.ColumnInfo>();
        columns.put("id", new ColumnInfo(false, null, 2));
        event2.setColumns(columns);
        eventsSent.add(event2);

        RowChangedEvent event3 = new RowChangedEvent();
        event3.setBinlog("dddd");
        event3.setBinlogPos(111);
        event3.setDatabase("test1");
        event3.setExecuteTime(1111122233);
        event3.setServerId(11111);
        event3.setSeq(111111222223334L);
        event3.setActionType(RowChangedEvent.UPDATE);
        event3.setTable("a");
        event3.setTransactionBegin(false);
        event3.setTransactionCommit(false);
        Map<String, ColumnInfo> columns3 = new HashMap<String, RowChangedEvent.ColumnInfo>();
        columns3.put("id", new ColumnInfo(false, 1, 2));
        event3.setColumns(columns3);
        eventsSent.add(event3);

        RowChangedEvent event4 = new RowChangedEvent();
        event4.setBinlog("dddd");
        event4.setBinlogPos(111);
        event4.setDatabase("test1");
        event4.setExecuteTime(1111122233);
        event4.setServerId(11111);
        event4.setSeq(111111222223334L);
        event4.setActionType(RowChangedEvent.DELETE);
        event4.setTable("a");
        event4.setTransactionBegin(false);
        event4.setTransactionCommit(false);
        Map<String, ColumnInfo> columns4 = new HashMap<String, RowChangedEvent.ColumnInfo>();
        columns4.put("id", new ColumnInfo(false, 1, null));
        event4.setColumns(columns4);
        eventsSent.add(event4);

        RowChangedEvent event5 = new RowChangedEvent();
        event5.setBinlog("dddd");
        event5.setBinlogPos(111);
        event5.setDatabase("test1");
        event5.setExecuteTime(1111122233);
        event5.setServerId(11111);
        event5.setSeq(111111222223334L);
        event5.setTransactionBegin(true);
        eventsSent.add(event5);

        RowChangedEvent event6 = new RowChangedEvent();
        event6.setBinlog("dddd");
        event6.setBinlogPos(111);
        event6.setDatabase("test1");
        event6.setExecuteTime(1111122233);
        event6.setServerId(11111);
        event6.setSeq(111111222223334L);
        event6.setTransactionCommit(true);
        eventsSent.add(event6);

        final List<Event> eventsReceived = new ArrayList<Event>();
        final List<Event> eventsSkipped = new ArrayList<Event>();

        StartMockPumaServer(eventsSent);

        PumaClient client = new PumaClient(configBuilder.build());

        client.register(new EventListener() {
            private int i = 0;

            @Override
            public void onSkipEvent(Event event) {
                eventsSkipped.add(event);
            }

            @Override
            public boolean onException(Event event, Exception e) {
                return true;
            }

            @Override
            public void onEvent(Event event) throws Exception {
                if (++i == 2) {
                    throw new Exception();
                }
                eventsReceived.add(event);
            }

            @Override
            public void onConnectException(Exception e) {
            }

            @Override
            public void onConnected() {

            }
            @Override
            public void onHeartbeatEvent(Event event){
            	
            }
        });

        client.start();

        Thread.sleep(5 * 1000);

        Assert.assertArrayEquals(Arrays.asList(new ChangedEvent[] { (ChangedEvent) eventsSent.get(1) }).toArray(new ChangedEvent[0]),
                eventsSkipped.toArray(new ChangedEvent[0]));
        eventsSent.remove(1);
        Assert.assertArrayEquals(eventsSent.toArray(new ChangedEvent[0]), eventsReceived.toArray(new ChangedEvent[0]));
    }

    /**
     * @param eventsSent
     */
    protected void StartMockPumaServer(final List<Event> eventsSent) {
        PumaThreadUtils.createThread(new Runnable() {

            @Override
            public void run() {
                try {
                    ServerSocket ss = new ServerSocket();
                    ss.bind(new InetSocketAddress(7862));
                    Socket s = ss.accept();
                    EventCodec codec = new JsonEventCodec();
                    for (Event event : eventsSent) {
                        byte[] bytes = codec.encode(event);
                        s.getOutputStream().write(ByteArrayUtils.intToByteArray(bytes.length));
                        s.getOutputStream().write(bytes);
                    }

                    s.close();
                    ss.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "mockServer", false).start();

    }

    private Object getValue(Object obj, String fieldName) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                try {
                    return field.get(obj);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }
}
