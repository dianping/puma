package com.dianping.puma.storage.cache

import com.dianping.puma.core.event.RowChangedEvent
import com.dianping.puma.storage.Sequence
import junit.framework.Assert
import org.junit.Test

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dozer @ 2015-12
 * mail@dozer.cc
 * http://www.dozer.cc
 */
class CachedDataStorageTest {

    private final CachedDataStorage target = new CachedDataStorage();

    private final CachedDataStorage.Reader reader1 = new CachedDataStorage.Reader(target);

    private final CachedDataStorage.Reader reader2 = new CachedDataStorage.Reader(target);

    @Test(expected = IOException.class)
    public void testNotStarted() throws Exception {
        Assert.assertFalse(reader1.open(null));
        reader1.next();
    }

    @Test(expected = IOException.class)
    public void testNotOpen() throws Exception {
        target.start();
        Assert.assertNull(reader1.next())
    }

    @Test
    public void testReadTooFast() throws Exception {
        target.start();
        ChangedEventWithSequence item1 = generateItem(1);
        ChangedEventWithSequence item2 = generateItem(2);

        target.append(item1);
        target.append(item2);

        reader1.open(item1.getSequence());
        reader2.open(item2.getSequence());

        Assert.assertNotNull(reader1.next())
        Assert.assertNotNull(reader1.next())
        Assert.assertNull(reader1.next())
        Assert.assertNotNull(reader2.next())
        Assert.assertNull(reader2.next())
    }

    @Test
    public void testReadMany() throws Exception {
        initTarget();

        for (int k = 1; k < 15000; k++) {
            ChangedEventWithSequence item = generateItem(k);
            target.append(item);
            Assert.assertNotNull(reader1.next());
            Assert.assertNotNull(reader2.next());
        }
    }

    @Test(timeout = 10000l)
    public void testMultiThreadWriteAndReadSuccess() throws Exception {
        initTarget()

        int count = 15000;

        AtomicBoolean writerSuccess = new AtomicBoolean();
        AtomicBoolean reader1Success = new AtomicBoolean();
        AtomicBoolean reader2Success = new AtomicBoolean();

        def writerThread = new Thread({
            for (int k = 1; k < count; k++) {
                ChangedEventWithSequence item = generateItem(k);
                target.append(item);
                System.out.print("write ${k}")
            }

            writerSuccess.set(true);
        });
        writerThread.start();

        def reader1Thread = new Thread({
            for (int k = 1; k < count; k++) {
                def item = null;
                while (item == null) {
                    item = reader1.next()
                    if (item != null) {
                        Assert.assertEquals(item.getSequence().getNumber(), k);
                        System.out.print("reader1 ${k}")
                    }
                }
            }

            reader1Success.set(true);
        });
        reader1Thread.start();

        def reader2Thread = new Thread({
            for (int k = 1; k < count; k++) {
                def item = null;
                while (item == null) {
                    item = reader2.next()
                    if (item != null) {
                        Assert.assertEquals(item.getSequence().getNumber(), k);
                        System.out.print("reader2 ${k}")
                    }
                }
            }

            reader2Success.set(true);
        });
        reader2Thread.start();

        while (writerThread.alive || reader1Thread.alive || reader2Thread.alive) {
            Thread.sleep(100);
        }

        Assert.assertTrue(writerSuccess.get());
        Assert.assertTrue(reader1Success.get());
        Assert.assertTrue(reader2Success.get());
    }


    @Test(timeout = 10000l)
    public void testMultiThreadWriteAndReadOutdated() throws Exception {
        initTarget()

        int count = 15000;

        AtomicBoolean writerSuccess = new AtomicBoolean();
        AtomicBoolean reader1Success = new AtomicBoolean();
        AtomicBoolean reader2Success = new AtomicBoolean();

        def writerThread = new Thread({
            for (int k = 1; k < count; k++) {
                ChangedEventWithSequence item = generateItem(k);
                target.append(item);
                System.out.print("write ${k}")
            }

            writerSuccess.set(true);
        });
        writerThread.start();

        def reader1Thread = new Thread({
            for (int k = 1; k < count; k++) {
                def item = null;
                while (item == null) {
                    item = reader1.next()
                    if (item != null) {
                        Assert.assertEquals(item.getSequence().getNumber(), k);
                        System.out.print("reader1 ${k}")
                    }
                }
            }

            reader1Success.set(true);
        });
        reader1Thread.start();

        def reader2Thread = new Thread({
            for (int k = 1; k < count; k++) {
                Thread.sleep(5);

                def item = null;
                while (item == null) {
                    item = reader2.next()
                    if (item != null) {
                        Assert.assertEquals(item.getSequence().getNumber(), k);
                        System.out.print("reader2 ${k}")
                    }
                }
            }

            reader2Success.set(true);
        });
        reader2Thread.start();

        while (writerThread.alive || reader1Thread.alive || reader2Thread.alive) {
            Thread.sleep(100);
        }

        Assert.assertTrue(writerSuccess.get());
        Assert.assertTrue(reader1Success.get());
        Assert.assertFalse(reader2Success.get());
    }

    @Test(expected = IOException.class)
    public void testVersionChanged() throws Exception {
        initTarget();
        target.stop();
        target.start();
        reader1.next();
    }

    @Test
    public void testOutDated() throws Exception {
        initTarget()

        for (int k = 1; k < 5001; k++) {
            ChangedEventWithSequence item = generateItem(k);
            target.append(item);
            Assert.assertNotNull(reader1.next());
        }

        try {
            reader2.next();
            Assert.fail("should not be here");
        } catch (IOException) {
            //should be here
        }
    }

    protected void initTarget() {
        ChangedEventWithSequence startItem = generateItem(0);
        target.start();
        target.append(startItem);
        reader1.open(startItem.sequence);
        Assert.assertNotNull(reader1.next());
        reader2.open(startItem.sequence);
        Assert.assertNotNull(reader2.next());
    }

    protected ChangedEventWithSequence generateItem(int pos) {
        new ChangedEventWithSequence(
                new RowChangedEvent(),
                new Sequence(19891111, pos)
        )
    }
}
