package com.dianping.puma.storage;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.status.QpsCounter;
import com.dianping.puma.storage.channel.ChannelFactory;
import com.dianping.puma.storage.channel.DefaultReadChannel;
import com.dianping.puma.storage.channel.ReadChannel;
import com.dianping.puma.storage.channel.WriteChannel;
import com.dianping.puma.storage.filesystem.FileSystem;
import com.dianping.puma.storage.utils.DateUtils;
import com.dianping.puma.utils.EventFactory;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StorageDebug extends StorageBaseTest {

    ReadChannel readChannel;

    WriteChannel writeChannel;

    QpsCounter qpsCounter = new QpsCounter(5);

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

    BlockingQueue<Event> events = new LinkedBlockingQueue<Event>(100000);

    @Test
    public void testAll() throws Exception {
        DateTime date = new DateTime();
        DateUtils.changeGetNowTime(date.toString("yyyyMMdd"));

        List<String> tablesNeeded = Lists.newArrayList("table1", "table2", "table3");
        List<String> tableNotNeeded = Lists.newArrayList("table4", "table5");
        List<String> alltables = Lists.newArrayList(tableNotNeeded);
        alltables.addAll(tablesNeeded);

        Random rnd = new Random();

        long count = 0;

        new Thread(new Reader(true)).start();
        new Thread(new Reader(false)).start();
        new Thread(new Reader(false)).start();
        new Thread(new Reader(false)).start();
        new Thread(new Reader(false)).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    System.out.println(qpsCounter.get(5));
                }
            }
        }).start();

        while (true) {
            count++;
            String table = alltables.get(rnd.nextInt(alltables.size()));
            ChangedEvent event = EventFactory.dml(System.currentTimeMillis(), 1, String.valueOf(count / 1000), count % 1000, "test", table, false, false, DMLType.INSERT);
            writeChannel.append(event);

            if (count % 30000000 == 0) {
                date = date.withFieldAdded(DurationFieldType.days(), 1);
                DateUtils.changeGetNowTime(date.toString("yyyyMMdd"));
            }

            if (tablesNeeded.contains(table)) {
                events.put(event);
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

    class Reader implements Runnable {
        private final boolean checkData;

        public Reader(boolean checkData) {
            this.checkData = checkData;
        }

        @Override
        public void run() {
            try {
                DefaultReadChannel readChannel;
                while (true) {
                    try {
                        readChannel = ChannelFactory.newReadChannel("test", Lists.newArrayList("table1", "table2", "table3"), true, false, false);
                        readChannel.start();
                        readChannel.openOldest();
                        break;
                    } catch (IOException e) {
                    }
                }

                while (true) {
                    Event event2;

                    while ((event2 = readChannel.next()) == null) {
                        Thread.sleep(1);
                    }

                    if (checkData) {
                        Event event1 = events.take();
                        assert event1.toString().equals(event2.toString());
                    }
                    qpsCounter.increase();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
