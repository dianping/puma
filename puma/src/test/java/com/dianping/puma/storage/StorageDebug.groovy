package com.dianping.puma.storage

import com.dianping.puma.core.event.Event
import com.dianping.puma.core.event.EventFactory
import com.dianping.puma.core.util.sql.DMLType
import com.dianping.puma.storage.channel.ChannelFactory
import com.dianping.puma.storage.channel.ReadChannel
import com.dianping.puma.storage.channel.WriteChannel
import com.dianping.puma.storage.filesystem.FileSystem
import com.dianping.puma.storage.utils.DateUtils
import com.google.common.collect.Lists
import org.joda.time.DateTime
import org.joda.time.DurationFieldType
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

public class StorageDebug extends StorageBaseTest {

    ReadChannel readChannel;

    WriteChannel writeChannel;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        FileSystem.changeBasePath(testDir.getAbsolutePath());

        writeChannel = ChannelFactory.newWriteChannel("test");
        writeChannel.start();

        readChannel = ChannelFactory.newReadChannel("test");
        readChannel.start();
    }

    BlockingQueue<Event> events = new LinkedBlockingQueue<Event>(10000);

    @Test
    public void testAll() throws Exception {
        def date = new DateTime()
        DateUtils.changeGetNowTime(date.toString("yyyyMMdd"))

        def tablesNeeded = ["table1", "table2", "table3"]
        def tableNotNeeded = ["table4", "table5"]
        def alltables = tableNotNeeded + tablesNeeded

        def rnd = new Random()

        long count = 0;

        new Thread({
            sleep(5000)

            def readChannel = ChannelFactory.newReadChannel("test", Lists.newArrayList("table1", "table2", "table3"), true, false, false)
            readChannel.start()
            readChannel.openOldest()

            while (true) {
                def event1 = events.take()

                def event2;

                while ((event2 = readChannel.next()) == null) {
                    sleep(1)
                }

                assert event1.toString().equals(event2.toString())
            }

        }).start();

        while (true) {
            count++
            def table = alltables.get(rnd.nextInt(alltables.size()))
            def event = EventFactory.dml(System.currentTimeMillis(), 1, (count / 1000).toString(), count % 1000, "test", table, false, false, DMLType.INSERT)
            writeChannel.append(event)

            if (count % 30000000 == 0) {
                date = date.withFieldAdded(DurationFieldType.days(), 1)
                DateUtils.changeGetNowTime(date.toString("yyyyMMdd"))
            }

            if (tablesNeeded.contains(table)) {
                events.put(event)
            }
        }
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
