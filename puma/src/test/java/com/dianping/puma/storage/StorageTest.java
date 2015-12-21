package com.dianping.puma.storage;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.storage.channel.ChannelFactory;
import com.dianping.puma.storage.channel.ReadChannel;
import com.dianping.puma.storage.channel.WriteChannel;
import com.dianping.puma.storage.filesystem.FileSystem;
import com.dianping.puma.storage.utils.DateUtils;
import com.dianping.puma.utils.EventFactory;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class StorageTest extends StorageBaseTest {

    ReadChannel readChannel;

    WriteChannel writeChannel;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        FileSystem.changeBasePath(testDir.getAbsolutePath());

        writeChannel = ChannelFactory.newWriteChannel("a");
        writeChannel.start();

        readChannel = ChannelFactory.newReadChannel("a");
        readChannel.start();
    }

    @Test
    public void testPage() throws IOException {
        DateUtils.changeGetNowTime("20150101");

        writeChannel.append(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT));
        DateUtils.changeGetNowTime("20150102");
        writeChannel.append(EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT));
        DateUtils.changeGetNowTime("20150103");
        writeChannel.append(EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.flush();

        readChannel.openOldest();
        assertEquals(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT), readChannel.next());
        assertEquals(EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT), readChannel.next());
        assertEquals(EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT), readChannel.next());


        DateUtils.changeGetNowTime(null);
    }

    @Test
    public void testPageFindByBinlog() throws IOException {
        DateUtils.changeGetNowTime("20150101");
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT));
        DateUtils.changeGetNowTime("20150102");
        writeChannel.append(EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT));
        DateUtils.changeGetNowTime("20150103");
        writeChannel.append(EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.flush();

        readChannel.open(new BinlogInfo(2, 1, "f.1", 2));
        assertEquals(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT), readChannel.next());
        assertEquals(EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT), readChannel.next());
        assertEquals(EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT), readChannel.next());


        DateUtils.changeGetNowTime(null);
    }

    /**
     * 1,1,1,1
     * 2,1,1,2
     * 3,1,1,3
     * <p/>
     * oldest
     * <p/>
     * 1,1,1,1
     *
     * @throws IOException
     */
    @Test
    public void testFindOldest() throws IOException {

        writeChannel.append(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.flush();

        readChannel.openOldest();
        assertEquals(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT), readChannel.next());
        assertEquals(EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT), readChannel.next());
        assertEquals(EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT), readChannel.next());

    }

    /**
     * 1,1,1,1
     * 2,1,1,2
     * 3,1,1,3
     * <p/>
     * latest
     * <p/>
     * 3,1,1,3
     *
     * @throws IOException
     */
    @Test
    public void testFindLatest() throws IOException {

        writeChannel.append(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT));

        writeChannel.flush();

        readChannel.openLatest();
        ChangedEvent binlogEvent = readChannel.next();

        assertTrue(binlogEvent.equals(EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT)));

    }

    /**
     * 1,1,1,1
     * 2,1,1,2
     * 3,1,1,3
     * <p/>
     * 2,1,1,2
     * <p/>
     * 2,1,1,2
     *
     * @throws IOException
     */
    @Test
    public void testFind0() throws IOException {

        writeChannel.append(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(2, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(3, 1, "f.1", 4, "a", "b", false, false, DMLType.INSERT));
        writeChannel.flush();

        readChannel.open(new BinlogInfo(2, 1, "f.1", 2));
        assertEquals(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT), readChannel.next());
        assertEquals(EventFactory.dml(2, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT), readChannel.next());
        assertEquals(EventFactory.dml(3, 1, "f.1", 4, "a", "b", false, false, DMLType.INSERT), readChannel.next());
    }

    /**
     * 1,1,1,1
     * 1,1,1,2
     * 1,1,1,3
     * <p/>
     * 1,1,1,2
     * <p/>
     * 1,1,1,2
     *
     * @throws IOException
     */
    @Test
    public void testFind1() throws IOException {

        writeChannel.append(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.flush();

        readChannel.open(new BinlogInfo(1, 1, "f.1", 2));
        ChangedEvent binlogEvent = readChannel.next();
        assertTrue(binlogEvent.equals(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT)));

    }

    /**
     * 1,1,1,1
     * 1,1,1,2
     * 1,1,1,3
     * <p/>
     * 1,1,1,4
     * <p/>
     * IOException
     *
     * @throws IOException
     */
    @Test
    public void testFind2() throws IOException {

        writeChannel.append(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.flush();

        readChannel.open(new BinlogInfo(1, 1, "f.1", 4));
        ChangedEvent binlogEvent = readChannel.next();
        assertTrue(binlogEvent.equals(EventFactory.dml(1, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT)));
    }

    /**
     * 1,1,1,1
     * 2,1,1,2
     * 3,1,1,3
     * 1,2,2,3
     * 3,2,2,8
     * <p/>
     * 3,2,2,8
     * <p/>
     * 3,1,1,3 or 3,2,2,8
     *
     * @throws IOException
     */
    @Test
    public void testServerIdChanged0() throws IOException {

        writeChannel.append(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 2, "f.2", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(3, 2, "f.2", 8, "a", "b", false, false, DMLType.INSERT));
        writeChannel.flush();

        readChannel.open(new BinlogInfo(3, 2, "f.2", 8));
        ChangedEvent binlogEvent = readChannel.next();
        assertTrue(binlogEvent.equals(EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT))
                || binlogEvent.equals(EventFactory.dml(1, 2, "f.2", 3, "a", "b", false, false, DMLType.INSERT)));

    }

    /**
     * 1,1,1,1
     * 2,1,1,2
     * 3,1,1,3
     * 1,2,2,3
     * 3,2,2,8
     * <p/>
     * 0,2,2,8
     * <p/>
     * 3,2,2,8
     *
     * @throws IOException
     */
    @Test
    public void testServerIdChanged1() throws IOException {

        writeChannel.append(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 2, "f.2", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(3, 2, "f.2", 8, "a", "b", false, false, DMLType.INSERT));
        writeChannel.flush();

        readChannel.open(new BinlogInfo(0, 2, "f.2", 8));
        ChangedEvent binlogEvent = readChannel.next();
        assertTrue(binlogEvent.equals(EventFactory.dml(1, 2, "f.2", 3, "a", "b", false, false, DMLType.INSERT)));
    }

    /**
     * 1,1,1,1
     * 2,1,1,2
     * 3,1,1,3
     * 1,2,2,3
     * 3,2,2,8
     * <p/>
     * 3,0,0,0
     * <p/>
     * 3,1,1,3 or 3,2,2,8
     *
     * @throws IOException
     */
    @Test
    public void testServerIdChanged2() throws IOException {

        writeChannel.append(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(3, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 2, "f.2", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(3, 2, "f.2", 8, "a", "b", false, false, DMLType.INSERT));

        writeChannel.flush();

        readChannel.open(new BinlogInfo(3, 0, "0", 0));
        ChangedEvent binlogEvent = readChannel.next();

        assertTrue(binlogEvent.equals(EventFactory.dml(2, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT))
                || binlogEvent.equals(EventFactory.dml(1, 2, "f.2", 3, "a", "b", false, false, DMLType.INSERT)));

    }

    @Test
    public void testFilterDatabase() throws Exception {
        ReadChannel readChannel
                = ChannelFactory.newReadChannel("a", Lists.newArrayList("b", "c"), true, true, false);
        readChannel.start();


        writeChannel.append(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 2, "b", "c", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 4, "c", "b", false, false, DMLType.INSERT));
        writeChannel.flush();

        readChannel.openOldest();
        assertEquals(EventFactory.dml(1, 1, "f.1", 1, "a", "b", false, false, DMLType.INSERT), getEventWithoutHeartbeat(readChannel));
        assertEquals(EventFactory.dml(1, 1, "f.1", 3, "a", "b", false, false, DMLType.INSERT), getEventWithoutHeartbeat(readChannel));

        assertNull(getEventWithoutHeartbeat(readChannel));

        readChannel.stop();
    }

    public ChangedEvent getEventWithoutHeartbeat(ReadChannel readChannel) throws IOException {
        while (true) {
            ChangedEvent event = readChannel.next();
            if (event == null) {
                return null;
            }

            if (!(event instanceof RowChangedEvent)) {
                return event;
            }

            RowChangedEvent rowChangedEvent = (RowChangedEvent) event;
            if (rowChangedEvent.getDmlType() == DMLType.NULL) {
                continue;
            }

            return event;
        }
    }

    @Test
    public void testFilterTable() throws Exception {
        ReadChannel readChannel
                = ChannelFactory.newReadChannel("a", Lists.newArrayList("b", "c"), true, true, false);
        readChannel.start();


        writeChannel.append(EventFactory.dml(1, 1, "f.1", 1, "a", "a", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 3, "a", "c", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 4, "a", "d", false, false, DMLType.INSERT));
        writeChannel.flush();

        readChannel.openOldest();
        assertEquals(EventFactory.dml(1, 1, "f.1", 2, "a", "b", false, false, DMLType.INSERT), getEventWithoutHeartbeat(readChannel));
        assertEquals(EventFactory.dml(1, 1, "f.1", 3, "a", "c", false, false, DMLType.INSERT), getEventWithoutHeartbeat(readChannel));

        assertNull(getEventWithoutHeartbeat(readChannel));

        readChannel.stop();
    }

    @Test
    public void testFilterDml() throws Exception {
        ReadChannel readChannel
                = ChannelFactory.newReadChannel("a", Lists.newArrayList("b", "c"), false, true, true);
        readChannel.start();


        writeChannel.append(EventFactory.ddl(1, 1, "f.1", 1, "a", "b"));
        writeChannel.append(EventFactory.ddl(1, 1, "f.1", 2, "a", "b"));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 3, "a", "c", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 4, "a", "b", true, false, DMLType.NULL));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 5, "a", "c", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 6, "a", "b", false, true, DMLType.NULL));
        writeChannel.flush();

        readChannel.openOldest();
        assertEquals(EventFactory.ddl(1, 1, "f.1", 1, "a", "b"), getEventWithoutHeartbeat(readChannel));
        assertEquals(EventFactory.ddl(1, 1, "f.1", 2, "a", "b"), getEventWithoutHeartbeat(readChannel));

        assertNull(getEventWithoutHeartbeat(readChannel));

        readChannel.stop();
    }

    @Test
    public void testFilterDdl() throws Exception {
        ReadChannel readChannel
                = ChannelFactory.newReadChannel("a", Lists.newArrayList("b", "c"), true, false, true);
        readChannel.start();


        writeChannel.append(EventFactory.ddl(1, 1, "f.1", 1, "a", "b"));
        writeChannel.append(EventFactory.ddl(1, 1, "f.1", 2, "a", "b"));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 3, "a", "c", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 4, "a", "b", true, false, DMLType.DELETE));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 5, "a", "c", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 6, "a", "b", false, true, DMLType.DELETE));
        writeChannel.flush();

        readChannel.openOldest();
        assertEquals(EventFactory.dml(1, 1, "f.1", 3, "a", "c", false, false, DMLType.INSERT), getEventWithoutHeartbeat(readChannel));
        assertEquals(EventFactory.dml(1, 1, "f.1", 4, "a", "b", true, false, DMLType.DELETE), getEventWithoutHeartbeat(readChannel));
        assertEquals(EventFactory.dml(1, 1, "f.1", 5, "a", "c", false, false, DMLType.INSERT), getEventWithoutHeartbeat(readChannel));
        assertEquals(EventFactory.dml(1, 1, "f.1", 6, "a", "b", false, true, DMLType.DELETE), getEventWithoutHeartbeat(readChannel));

        assertNull(getEventWithoutHeartbeat(readChannel));

        readChannel.stop();
    }

    @Test
    public void testFilterTransaction() throws Exception {
        ReadChannel readChannel
                = ChannelFactory.newReadChannel("a", Lists.newArrayList("b", "c"), true, true, false);
        readChannel.start();


        writeChannel.append(EventFactory.ddl(1, 1, "f.1", 1, "a", "b"));
        writeChannel.append(EventFactory.ddl(1, 1, "f.1", 2, "a", "b"));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 3, "a", "c", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 4, "a", "b", true, false, DMLType.NULL));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 5, "a", "c", false, false, DMLType.INSERT));
        writeChannel.append(EventFactory.dml(1, 1, "f.1", 6, "a", "b", false, true, DMLType.NULL));
        writeChannel.flush();

        readChannel.openOldest();
        assertEquals(EventFactory.ddl(1, 1, "f.1", 1, "a", "b"), getEventWithoutHeartbeat(readChannel));
        assertEquals(EventFactory.ddl(1, 1, "f.1", 2, "a", "b"), getEventWithoutHeartbeat(readChannel));
        assertEquals(EventFactory.dml(1, 1, "f.1", 3, "a", "c", false, false, DMLType.INSERT), getEventWithoutHeartbeat(readChannel));
        assertEquals(EventFactory.dml(1, 1, "f.1", 5, "a", "c", false, false, DMLType.INSERT), getEventWithoutHeartbeat(readChannel));

        assertNull(getEventWithoutHeartbeat(readChannel));

        readChannel.stop();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        if (writeChannel != null) {
            writeChannel.stop();
        }

        if (readChannel != null) {
            readChannel.stop();
        }

        super.tearDown();

        FileSystem.changeBasePath(FileSystem.DEFAULT_PATH);
    }
}
